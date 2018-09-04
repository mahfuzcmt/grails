package com.webcommander.plugin.enterprise_variation

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.admin.Operator
import com.webcommander.annotations.Initializable
import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.common.Resource
import com.webcommander.common.VideoService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.conversion.LengthConversions
import com.webcommander.conversion.MassConversions
import com.webcommander.events.AppEventManager
import com.webcommander.item.ImportConf
import com.webcommander.item.ProductRawData
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.enterprise_variation.constants.EnterpriseProperties
import com.webcommander.plugin.enterprise_variation.models.EnterpriseVariationProductData
import com.webcommander.plugin.variation.OrderVariationItem
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.factory.VariationObjectsProducer
import com.webcommander.plugin.variation.models.ProductVariationRawData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.plugin.variation.models.VariationServiceModel
import com.webcommander.task.Task
import com.webcommander.task.TaskLogger
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.io.FilenameUtils
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

@Initializable
class EnterpriseVariationService implements VariationServiceModel {
    @Autowired
    @Qualifier(value = "com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    org.grails.plugins.web.taglib.ApplicationTagLib g
    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib appResource
    ProductService productService
    ImageService imageService
    VideoService videoService
    FileService fileService
    SessionFactory sessionFactory
    CommonImportService commonImportService
    ProductImportService productImportService

    private static EnterpriseVariationService instance = null

    static void initialize() {
        AppEventManager.on("before-enterprise-details-delete", { id ->
            EvariationDetails eDetails = EvariationDetails.get(id)
            if(eDetails) {
                eDetails.inventoryHistory*.delete()
            }
        })

        AppEventManager.on("before-enterprise-option-delete", { id ->
            def description = EvariationDescription.get(id)
            description.delete()
        })

        AppEventManager.on("before-enterprise-variation-details-delete", { id ->
            EvariationDetails eDetails = EvariationDetails.get(id)
            if(eDetails) {
                AppEventManager.fire("before-enterprise-details-delete", [eDetails.id])
                eDetails.delete([flush: true])
            }
        })

        AppEventManager.on("before-enterprise-details-delete", { id ->
            EvariationDetails details = EvariationDetails.get(id)
            details.options*.delete()
            if(details.options.description) {
                details.options.description*.delete()
            }
        })
    }

    public static getInstance() {
        if(!instance) {
            instance = Holders.applicationContext.getBean("enterpriseVariationService")
        }
        return instance
    }

    static {
        HookManager.register("resolveLoyaltyPointVariation", { id, ProductData data ->
            VariationService variationService = VariationService.getInstance()
            if(variationService.allowedEnterprise()) {
                def selected = data.attrs.selectedVariation
                if(selected) {
                    def details = ProductVariation.get(selected).details
                    return details.id
                }
            }
            return id
        })

        HookManager.register("resolveVariationForCartItem", {Long id, CartItem cartItem ->
            if(cartItem.object instanceof VariationProductInCart && cartItem.object.product.attrs.selectedVariation) {
                def variation = ProductVariation.get(cartItem.object.product.attrs.selectedVariation)
                if(variation) {
                    id = variation.details.modelId
                }
            }
            return id
        })

        HookManager.register("resolveVariationForOrderItem", {Long id, OrderItem orderItem ->
            OrderVariationItem item
            if((item = OrderVariationItem.findByOrderItem(orderItem))) {
                def variation = ProductVariation.get(item.variationId)
                if(variation) {
                    id = variation.details.modelId
                }
            }
            return id
        })

        HookManager.register("productDetailsPageProduct", { Product product, Map params ->
            VariationService variationService = VariationService.getInstance()
            if(variationService.allowedEnterprise() && !product) {
                def details = EvariationDetails.findByUrl(params.url)
                if (details) {
                    ProductVariation variation = details.findProductVariation()
                    product = variation.product
                    Map config = [variation: variation.id]
                    params.config = config
                }
            }
            return product
        })

        HookManager.register("productSharedProperty", { response, type, ProductData data ->
            def id = data.attrs.selectedVariation
            if(id) {
                def variation = ProductVariation.get(id)
                def details = EvariationDetails.get(variation.details.modelId)
                if(!details) {
                    return response
                }
                switch(type) {
                    case "spec-url":
                        if(details.spec) {
                            response = "resources/variation/product/product-" + details.id
                        }
                        break
                }
            }
            return response
        })

        AppEventManager.on("after-enterprise-variation-create after-enterprise-variation-option-attach after-enterprise-variation-option-detach", {VariationDetails details->
            Product product = details.product
            def eDetails = EvariationDetails.get(details.modelId) ?: new EvariationDetails()
            if(!eDetails.id) {
                String suffix = "_" + details.id
                eDetails.name = product.name + suffix
                eDetails.sku = product.sku + suffix
                eDetails.url = product.url + suffix
                eDetails.save()
                details.modelId = eDetails.id
                details.save()
            }
        })

        AppEventManager.on("before-enterprise-details-delete", { id ->
            EvariationDetails details = EvariationDetails.get(id)
            EnterpriseVariationService service = EnterpriseVariationService.getInstance()
            if(details.spec) {
                File filePath = new File(service.processFilePath(details.id) + "/spec")
                if (filePath.exists() && filePath.deleteDir()) {
                    details.spec = null
                }
            }

            List<Long> imageIds = details.images.id.flatten()
            if (imageIds) {
                details.images = []
                service.removeVariationImages(imageIds, details.id)
            }

            List<Long> videosIds = details.videos.id.flatten()
            List<String> videoNames = details.videos.name.flatten()
            boolean success;
            if (videosIds) {
                details.videos = []
                success = VariationProductVideo.where {
                    id in videosIds
                }.deleteAll() > 0
                if (success) {
                    String baseDir = "resources/variation/product/product-${details.id}/"
                    service.deleteVideos(videoNames, baseDir)
                }
            }

            List<MetaTag> metaTags = details.metaTags
            details.metaTags = []
            metaTags*.delete()
        })

        HookManager.register("before-update-stock", { updated, Product product, Map config, quantity, note ->
            VariationService variationService = VariationService.instance
            ProductVariation variation = null
            OrderVariationItem variationItem = OrderVariationItem.createCriteria().get {
                eq("orderItem.id", config.orderItemId.toLong())
            }
            if(variationItem) {
                variation = ProductVariation.get(variationItem.variationId)
            } else if(config.variations) {
                variation = variationService.getVariationByOptionList(product, config.variations)
            }
            if(variation?.details && variation.details.model == "enterprise") {
                EvariationDetails eDetails = EvariationDetails.get(variation.details.modelId)
                if(eDetails?.isInventoryEnabled) {
                    VariationInventoryHistory history = new VariationInventoryHistory(note: note, evariationDetails: eDetails)
                    history.changeQuantity = quantity * -1
                    history.save()
                    eDetails.availableStock = eDetails.availableStock - quantity
                    eDetails.save()
                    AppEventManager.fire("details-update", [product.id])
                }
                updated = true
            } else if(variationItem && variationItem.variationModel == "enterprise") {
                updated = true
            }
            return updated
        })
    }

    private Boolean isInvalid(String attr, def value, Long self) {
        return Product.createCriteria().count {
            eq(attr, value)
        } + EvariationDetails.createCriteria().count {
            eq(attr, value)
            ne("id", self)
        }
    }

    @Transactional
    private void preProcessSave(EvariationDetails details, String type) {
        List<EvariationDetailsOption> options = details.options.findAll {it.field.startsWith(type)}
        options.each {
            details.removeFromOptions(it)
            it.delete()
            if(it.description) {
                it.description.delete()
            }
        }
    }

    Integer getInventoryHistoryCount(Map params) {
        Long id = params.long("id");
        Integer count = 0;
        def details = EvariationDetails.get(id)
        if(details) {
            count = details.inventoryHistory.size()
        }
        return count;
    }

    List getInventoryHistory(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        List<VariationInventoryHistory> histories = VariationInventoryHistory.createCriteria().list(listMap) {
            eq("evariationDetails.id", params['id'].toLong())
            order("id", "desc")
        }
        return histories;
    }

    @Transactional
    Boolean saveBasic(GrailsParameterMap params) {
        String type = "basic."
        Map attrs = params.basic ?: [:]
        def eDetails = EvariationDetails.get(params.id)
        if(params.sku && isInvalid("sku", params.sku, eDetails.id)) {
            throw new ApplicationRuntimeException("product.sku.exists", "alert")
        }
        if(params.url && isInvalid("url", params.url, eDetails.id)) {
            throw new ApplicationRuntimeException("product.url.exists", "alert")
        }
        preProcessSave(eDetails, type)
        eDetails.name = params.name
        eDetails.sku = params.sku
        eDetails.url = params.url
        attrs.each {
            EvariationDetailsOption option = new EvariationDetailsOption(field: type + it.key)
            if(it.key == "description") {
                option.description = new EvariationDescription(content: it.value).save()
            } else {
                option.value = it.value
            }
            eDetails.addToOptions(option)
        }
        eDetails.save()
        return !eDetails.hasErrors()
    }

    @Transactional
    Boolean savePriceStock(GrailsParameterMap params) {
        String type = "priceStock."
        Map attrs = params.priceStock ?: [:]
        if (attrs.minOrderQuantity != null && attrs.minOrderQuantity.equals("")) {
            attrs.minOrderQuantity = "1";
        }
        def eDetails = EvariationDetails.get(params.id)
        preProcessSave(eDetails, type)
        eDetails.isInventoryEnabled = (params.isInventoryEnabled == 'true')
        if(eDetails.isInventoryEnabled && params.changeQuantity) {
            Integer changeQuantity = params.int("changeQuantity")
            String note = params.note ?: null
            Operator createdBy = Operator.get(AppUtil.session.admin)
            if (changeQuantity != 0) {
                VariationInventoryHistory vih = new VariationInventoryHistory(
                    changeQuantity: changeQuantity,
                    createdBy: createdBy,
                    note: note,
                    evariationDetails: eDetails
                ).save()
                eDetails.availableStock += changeQuantity;
                eDetails.addToInventoryHistory(vih)
            }
        }
        eDetails.lowStockLevel = params.lowStockLevel ? params['lowStockLevel'].toInteger() : 0
        def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
        String unitLength = generalSettings.unit_length;
        String unitWeight = generalSettings.unit_weight;
        if (attrs.isExpectToPay == "true") {
            Double basePrice = eDetails.options.find { it.field == "basic.basePrice" }?.value?.toDouble() ?: eDetails.findProductVariation().product.basePrice
            if(attrs.expectToPayPrice.toDouble() <= basePrice) {
                throw new ApplicationRuntimeException("x.should.greater.then.y", [g.message(code: "expect.to.pay"), g.message(code: "base.price")])
            }
            attrs.isOnSale = "false"
        }
        attrs.each {
            def tempOption = new EvariationDetailsOption(field: type + it.key)
            try {
                switch (it.key) {
                    case "length":
                        tempOption.value = "" + LengthConversions.convertLengthToSI(unitLength, it.value.toDouble()).toDouble()
                        break
                    case "width":
                        tempOption.value = "" + LengthConversions.convertLengthToSI(unitLength, it.value.toDouble()).toDouble()
                        break
                    case "height":
                        tempOption.value = "" + LengthConversions.convertLengthToSI(unitLength, it.value.toDouble()).toDouble()
                        break
                    case "weight":
                        tempOption.value = "" + MassConversions.convertMassToSI(unitWeight, it.value.toDouble()).toDouble()
                        break
                    default:
                        tempOption.value = it.value
                        break
                }
            } catch (NumberFormatException number) {

            }
            eDetails.addToOptions(tempOption)
        }
        Map resp = [status: true]
        resp = HookManager.hook("savePriceNQuantity", resp, params)
        eDetails.save()
        return !eDetails.hasErrors()
    }

    @Transactional
    Boolean saveImage(GrailsParameterMap params) {
        def eDetails = EvariationDetails.get(params.id)
        Boolean success
        if((success = updateImages(eDetails, params))) {
            List images = AppUtil.session.request.getMultiFileMap().images
            if(images && images.size()) {
                success = addImages(eDetails, images);
            }
        }
        return success
    }

    @Transactional
    Boolean addImages(EvariationDetails eDetails, List<MultipartFile> images) {
        String filePath = processFilePath(eDetails.id)
        Integer currentImages = eDetails.images?.size() ?: 0
        images.each {
            String name = productService.processImageName(filePath, it.originalFilename)
            VariationProductImage image = new VariationProductImage(name: name, idx: ++currentImages)
            image.evariationDetails = eDetails
            imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE, image)
            eDetails.addToImages(image)
        }
        eDetails.save()
        if (!eDetails.hasErrors()) {
            return true
        }
        return false
    }

    String processFilePath(Long eDetailsId) {
        String filePath = PathManager.getResourceRoot("variation/product/product-${eDetailsId}");
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return filePath
    }

    @Transactional
    Boolean updateImages(EvariationDetails eDetails, GrailsParameterMap params) {
        if (!eDetails) {
            return false;
        }
        List<Long> imgIds = params.list("remove-images")*.toLong();
        boolean success = removeVariationImages(imgIds, eDetails.id);
        List orderSeq = params.list("imageId");
        List ids = params.list("altTextId")
        List altTexts = params.list("altText");
        ids.eachWithIndex { def entry, int i ->
            VariationProductImage img = VariationProductImage.get(entry)
            img.altText = altTexts[i]
        }
        orderSeq.eachWithIndex { def entry, int i ->
            VariationProductImage img = VariationProductImage.get(entry)
            img.idx = i + 1;
            img.merge(flush: true)
        }
        if (success) {
            sessionFactory.cache.evictCollectionRegions();
            return true
        }
        return false
    }

    @Transactional
    public Boolean removeVariationImages(List<Long> imgIds, Long eDetailsId) {
        boolean success;
        if (imgIds.size() > 0) {
            def dCriteria = VariationProductImage.where {
                id in imgIds
            }
            List<VariationProductImage> variationImages = dCriteria.list();
            List<String> names = variationImages?.name
            success = dCriteria.deleteAll() > 0
            if (success) {
                def namePrefix = ["", "150-", "300-", "450-", "600-", "900-"]
                names.each { name ->
                    namePrefix.each {
                        File imgFile = new File(PathManager.getResourceRoot("variation/product/product-${eDetailsId}/${it + name}"))
                        imgFile.delete();
                    }
                }
                File dir = new File(PathManager.getResourceRoot("variation/product/product-${eDetailsId}"))
                if(dir.exists()) {
                    dir.delete()
                }
            }
        } else {
            success = true;
        }
        return success
    }

    @Transactional
    Boolean saveVideo(GrailsParameterMap params) {
        Boolean success
        def eDetails = EvariationDetails.get(params.id)
        if ((success = updateVideos(eDetails, params))) {
            def videoList = []
            if(params.videos) {
                videoList.addAll(params.videos)
                success = addVideos(eDetails, videoList);
            }
        }
        return success
    }

    @Transactional
    boolean addVideos(EvariationDetails eDetails, List<MultipartFile> videos) {
        def filePath = PathManager.getResourceRoot("variation/product/product-${eDetails.id}");
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Integer currentVideos = eDetails.videos.size()
        videos.each {
            String name = FilenameUtils.getBaseName(it.originalFilename);
            String attempt = name;
            Integer tryCount = 0;
            String extension = FilenameUtils.getExtension(it.originalFilename);
            while (true) {
                File targetFile = new File(filePath, attempt + "." + extension)
                if (!targetFile.exists()) {
                    break;
                }
                attempt = name + "_" + (++tryCount);
            }
            name = attempt + "." + extension;
            videoService.uploadVideo(it, filePath, extension, name, 50 * 1024 * 1024)
            VariationProductVideo video = new VariationProductVideo(name: name, title: name, idx: ++currentVideos)
            eDetails.addToVideos(video)
        }
        eDetails.merge()
        return !eDetails.hasErrors();
    }

    @Transactional
    boolean updateVideos(EvariationDetails eDetails, Map params) {
        if (!eDetails) {
            return false;
        }
        List<Long> videosIds = params.list("remove-videos")*.toLong();
        List<String> names = videosIds.size() > 0 ? VariationProductVideo.where {
            id in videosIds
        }.list().name : [];

        boolean success;
        if (videosIds.size() > 0) {
            success = VariationProductVideo.where {
                id in videosIds
            }.deleteAll() > 0
            if (success) {
                String baseDir = "resources/variation/product/product-${eDetails.id}/"
                deleteVideos(names, baseDir)
            }
        } else {
            success = true;
        }
        if (success) {
            return true
        }
        return false
    }

    void deleteVideos(List names, String baseDir) {
        names.each { name ->
            File video = new File(Holders.servletContext.getRealPath(baseDir + name))
            File thumbImage = new File(Holders.servletContext.getRealPath(baseDir + "video-thumb/" + FilenameUtils.getBaseName(name) + ".jpg"))
            if(video.exists()) {
                video.delete()
            }
            if(thumbImage.exists()) {
                thumbImage.delete()
            }
        }
    }

    @Transactional
    boolean updateProductFile(Map params, MultipartFile uploadFile) {
        EvariationDetails eDetails = EvariationDetails.get(params.id)
        String productOutdatedRelativeFilePath = appResource.getDownloadableProductTypeFileUrl(productId: eDetails.id, productFileName: eDetails?.productFile?.name, isVariationFile: true)
        if (params.fileRemoved == "true") {
            eDetails.productFile = null
            fileService.removeModifiableResource(productOutdatedRelativeFilePath)
            eDetails.merge()
            return true
        } else if (uploadFile) {
            if(eDetails.productFile){
                fileService.removeModifiableResource(productOutdatedRelativeFilePath)
            }
            else {
                eDetails.productFile = new Resource()
            }
            fileService.removeModifiableResource(productOutdatedRelativeFilePath)
            String fileName = uploadFile?.originalFilename
            String productUpdatedRelativeFilePath = appResource.getDownloadableProductTypeFileUrl(productId: eDetails.id, productFileName: fileName, isVariationFile: true)
            eDetails.productFile.name = fileName
            eDetails.productFile.cloudConfig = fileService.putProductDownloadableFile(uploadFile.inputStream, productUpdatedRelativeFilePath)
        } else {
            return false
        }
        eDetails.merge()
        return true
    }

    @Transactional
    boolean specUpload(EvariationDetails eDetails, MultipartFile specFile) {
        Map params = AppUtil.params
        File filePath = new File(processFilePath(eDetails.id) + "/spec")
        if(params["remove_spec"]){
            eDetails.spec = null
            eDetails.merge()
            if (filePath.exists() && filePath.deleteDir()) {
                return true
            }
        }
        if(specFile) {
            String fileName = specFile.originalFilename
            if (fileName.lastIndexOf('.') < 0 ) {
                throw new Exception("file.should.contain.extension")
            }
            if(fileName) {
                if (!eDetails.spec) {
                    eDetails.spec = new Resource()
                }
                if(filePath.exists()) {
                    filePath.deleteDir()
                }
                if(!filePath.exists()) {
                    filePath.mkdir()
                }
                fileService.uploadFile(specFile, NamedConstants.RESOURCE_TYPE.RESOURCE, fileName, eDetails.spec, filePath.toString())
                eDetails.spec.name = fileName
                try {
                    eDetails.merge()
                } catch(Exception e) {
                    filePath.deleteDir()
                    return false
                }
            }
            return !eDetails.hasErrors()
        } else {
            return false
        }
    }

    @Transactional
    Boolean saveAdvanced(GrailsParameterMap params) {
        def eDetails = EvariationDetails.get(params.id)
        String type = "advanced."
        Map attrs = params.advanced ?: [:]
        preProcessSave(eDetails, type)
        eDetails.metaTags*.delete()
        eDetails.metaTags = []
        if(params.enabeMetatag) {
            List names = params.list("tag_name")
            List values = params.list("tag_content")
            names.eachWithIndex{ name, i ->
                MetaTag mt = new MetaTag(name: name, value: values[i]).save()
                eDetails.addToMetaTags(mt)
            }
        }
        attrs.each {
            def tempOption = new EvariationDetailsOption(field: type + it.key, value: it.value)
            eDetails.addToOptions(tempOption)
        }
        eDetails.save()
        Map response = [
            success: !eDetails.hasErrors()
        ]
        params.target = "variation"
        params.loyaltyPoint =  [id: eDetails.id, target: "variation", point: params.loyaltyPoint]
        HookManager.hook("saveCategoryAdvancedData", response, params)
        return response.success
    }

    Map getDataForAPI(Map data, ProductVariation variation) {
        VariationService variationService  = VariationService.getInstance()
        if(!variationService.allowedEnterprise()) {
            return data
        }
        def eDetails = EvariationDetails.get(variation.details.modelId)
        if(eDetails) {
            Product product = variation.product
            EnterpriseVariationProductData variationProductData = VariationObjectsProducer.getVariationProductData(product, [variation: variation.id])
            data.name = eDetails.name
            data.sku = eDetails.sku
            data.url = eDetails.url
            data.metaTags = eDetails.metaTags ?: product.metaTags

            EnterpriseProperties.CORE_PROP.each {
                data[it] = variationProductData[it]
            }
            EnterpriseProperties.BOOLEAN_PROP.each {
                data[it] = variationProductData[it]
            }
            EnterpriseProperties.STRING_PROP.each {
                data[it] = variationProductData[it]
            }
            EnterpriseProperties.DOUBLE_PROP.each {
                data[it] = variationProductData[it]
            }
            data.displayPrice = variationProductData.isPriceRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)  ? null : variationProductData.priceToDisplay
            data.previousPrice = variationProductData.isPriceOrPurchaseRestricted(AppUtil.loggedCustomer, AppUtil.loggedCustomerGroupIds)  ? null : variationProductData.previousPriceToDisplay
            data.minOrderQuantity = variationProductData.minOrderQuantity
            data.maxOrderQuantity = variationProductData.maxOrderQuantity
            data.multipleOfOrderQuantity = variationProductData.multipleOfOrderQuantity
            data.supportedMaxOrderQuantity = variationProductData.supportedMaxOrderQuantity
            List images = eDetails.images ?: product.images
            data.images = []
            images.each {
                Map imageMap = [:]
                imageMap["id"] = it.id
                imageMap["thumbnail"] =  app.baseUrl() + it.findUrlInfix() + "150-" + it.name;
                imageMap["url"] = app.baseUrl() + it.findUrlInfix() + it.name;
                data.images.add(imageMap)
            }
            data.videos  = eDetails.videos ?: product.videos
        }
        return data
    }

    def castRawData(ProductRawData rawData, String field, Task task) {
        TaskLogger taskLogger = task.taskLogger
        String matchByData = "Enterprise Variation: " + (rawData.name ?: "")
        switch (field) {
            case "name":
                return rawData.name
            case "sku":
                return rawData.sku
            case "summary":
                String fieldName = "summary"
                if (rawData.summary) {
                    return rawData.summary
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "description":
                String fieldName = "description"
                if (rawData.description) {
                    return rawData.description
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "basePrice":
                String fieldName = "base.price"
                if (rawData.basePrice) {
                    return rawData.basePrice.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "costPrice":
                String fieldName = "cost.price"
                if (rawData.costPrice) {
                    return rawData.costPrice.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "salePrice":
                String fieldName = "sale.price"
                if (rawData.salePrice) {
                    return rawData.salePrice.toDouble()
                } else {
                    return 0.0
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "isCallForPriceEnabled":
                String fieldName = "call.for.price"
                if (rawData.isCallForPriceEnabled) {
                    return productImportService.isCallForPriceEnabled(rawData.isCallForPriceEnabled)
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return false
            case "isInventoryEnabled":
                String fieldName = "inventory.tracking"
                if (rawData.isInventoryEnabled) {
                    return productImportService.isInventoryEnabled(rawData.isInventoryEnabled)
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return false
            case "availableStock":
                String fieldName = "available.stock"
                if (rawData.availableStock) {
                    return Double.valueOf(rawData.availableStock).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "lowStockLevel":
                String fieldName = "low.stock.level"
                if (rawData.lowStockLevel) {
                    return rawData.lowStockLevel.toInteger()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "minOrderQuantity":
                String fieldName = "minimum.order.quantity"
                if (rawData.minOrderQuantity) {
                    return Double.valueOf(rawData.minOrderQuantity).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "maxOrderQuantity":
                String fieldName = "maximum.order.quantity"
                if (rawData.maxOrderQuantity) {
                    return Double.valueOf(rawData.maxOrderQuantity).intValue()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "model":
                String fieldName = "model"
                if (rawData.model) {
                    return rawData.model
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "height":
                String fieldName = "height"
                if (rawData.height) {
                    return rawData.height.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "width":
                String fieldName = "width"
                if (rawData.width) {
                    return rawData.width.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "weight":
                String fieldName = "weight"
                if (rawData.weight) {
                    return rawData.weight.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "length":
                String fieldName = "length"
                if (rawData.length) {
                    return rawData.length.toDouble()
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return 0
            case "metaTags":
                String fieldName = "meta.tag"
                if (rawData.metaTags) {
                    List<MetaTag> metaTags = commonImportService.generateMetaTags(rawData.metaTags)
                    if (metaTags.size()) {
                        return metaTags
                    } else {
                        commonImportService.dataNotFoundWarning(fieldName, taskLogger, matchByData)
                        task.meta.productWarningCount++
                    }
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            case "videos":
                String fieldName = "video"
                if (rawData.videos) {
                    return null
                }
                commonImportService.emptyFieldWarning(fieldName, taskLogger, matchByData)
                task.meta.productWarningCount++
                return null
            default:
                return null
        }
    }

    def saveVariationImages(EvariationDetails details, String images, String imageSource, Task task, String matchByData) {
        String filePath = processFilePath(details.id)
        File imgDir = new File(filePath)
        if (imgDir.exists()) {
            Set<String> prefixes = [""] as HashSet //for base image
            prefixes.addAll(imageService.getSizes(NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE).keySet().collect { it + "-" })
            VariationProductImage.createCriteria().list {
                eq("evariationDetails", details)
            }.each { VariationProductImage it ->
                try {
                    String fileName = it.name
                    File file = new File(filePath + "/" + fileName)
                    if(file.exists()) file.delete()
                    prefixes.each { prefix ->
                        file = new File(filePath + "/" + prefix + fileName)
                        file.delete()
                    }
                    it.delete()
                } catch (FileNotFoundException ex) {
                    log.error("Error processing on : " + ex)
                } catch (Exception ex) {
                    log.error("Error processing on : " + ex)
                }
            }
        }
        Integer lastImageIdx = 0
        List<String> imageList = images.split(',')
        imageList.each {
            String name = it.trim()
            InputStream inputStream = commonImportService.findImageStream(name, imageSource)
            if (inputStream) {
                try {
                    name = productService.processImageName(filePath, name)
                    imageService.createCopies(inputStream, name, filePath, imageService.getSizes(NamedConstants.IMAGE_RESIZE_TYPE.PRODUCT_IMAGE))
                } catch (Exception exc) {
                    log.error("Copy Import variation Image Error: " + exc)
                    task.taskLogger.warning(matchByData, "variation.image.copy.error")
                    task.meta.productWarningCount++
                    return
                }
                VariationProductImage productImage = new VariationProductImage(name: name, evariationDetails: details, idx: ++lastImageIdx)
                try {
                    productImage.save()
                } catch (Exception exc) {
                    log.error("Product Image Save Error: " + exc)
                    task.taskLogger.warning(matchByData, "variation.image.save.error")
                    task.meta.productWarningCount++
                    return
                }
                details.addToImages(productImage)
            } else {
                task.taskLogger.warning(matchByData, "variation.image.save.error")
                task.meta.productWarningCount++
            }
        }
    }

    @Override
    Boolean importVariation(ProductVariation variation, ProductVariationRawData variationRawData, ImportConf conf, Task task) {
        VariationDetails variationDetails = variation.details
        EvariationDetails eVariationDetails =  EvariationDetails.get(variationDetails.modelId)
        ProductRawData rawData = variationRawData.rawData
        String matchByData = "Enterprise Variation: " + (rawData.name ?: "")
        if(eVariationDetails) {
            eVariationDetails.name = castRawData(rawData, "name", task) ?: eVariationDetails.name
            String sku = castRawData(rawData, "sku", task) ?: eVariationDetails.sku
            if(sku && isInvalid("sku", sku, eVariationDetails.id)) {
                task.taskLogger.warning(matchByData, "details.sku.exist")
            } else {
                eVariationDetails.sku = sku ?: eVariationDetails.sku
            }

            EnterpriseProperties.CORE_PROP.each { String field ->
                if(conf.fieldsMap[field]) {
                    eVariationDetails."$field" = castRawData(rawData, field, task)
                }
            }

            Closure closure = {String type, String field ->
                if(conf.fieldsMap[field]) {
                    EvariationDetailsOption detailsOption = eVariationDetails.options.find {
                        it.field == field
                    }
                    detailsOption = detailsOption ?: new EvariationDetailsOption(field: type + field)
                    def value = castRawData(rawData, field, task)
                    if(field == "description") {
                        EvariationDescription description  = detailsOption.description ?: new EvariationDescription()
                        description.content = value?.toString()
                        detailsOption.description = description.save()
                    } else {
                        detailsOption.value = value?.toString()
                    }
                    eVariationDetails.addToOptions(detailsOption)
                    if(field == "salePrice") {
                        detailsOption = eVariationDetails.options.find {
                            it.field == "priceStock.isOnSale"
                        }
                        detailsOption = detailsOption ?: new EvariationDetailsOption(field: "priceStock.isOnSale")
                        detailsOption.value = (value > 0).toString()
                    }
                    eVariationDetails.addToOptions(detailsOption)
                }
            }

            EnterpriseProperties.BASIC_PROP.each { String field ->
                closure("basic.", field)
            }

            EnterpriseProperties.PRICE_STOCK_PROP.each { String field ->
                closure("priceStock.", field)
            }

            EnterpriseProperties.ADVANCE_PROP.each { String field ->
                closure("advanced.", field)
            }
            if(rawData.image) {
                saveVariationImages(eVariationDetails, rawData.image, conf.imageSource, task, matchByData)
            }
            eVariationDetails.save()
            return true
        }
        return false
    }

}

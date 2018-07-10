package com.webcommander.plugin.variation

import com.webcommander.ApplicationTagLib
import com.webcommander.annotations.Initializable
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.installation.template.TemplateContent
import com.webcommander.models.DownloadableProductInCart
import com.webcommander.plugin.PluginManager
import com.webcommander.plugin.variation.constant.DomainConstants as DC
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.plugin.variation.util.VariationUtils
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FileUtils
import grails.util.TypeConvertingMap
import org.hibernate.SessionFactory
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

@Initializable
class VariationService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    ImageService imageService
    SessionFactory sessionFactory
    private static VariationService instance = null

    static void initialize() {
        HookManager.register("variation-option-delete-veto", { response, id ->
            List ids = ProductVariation.createCriteria().list {
                projections {
                    property("product.id")
                }
                options {
                    eq("id", id)
                }
            }.unique()
            if(ids) {
                response.product = ids.size()
            }
            return response
        })

        HookManager.register("productInventoryManipulation", { response, inventoryManipulation ->
            Product product = inventoryManipulation.product
            ProductVariation variation = ProductVariation.findByProduct(product)
            if(variation && variation.active) {
                response.isInventoryEnable = false
            }
            return response
        })

        HookManager.register("variationOption-delete-veto-list", { response, id ->
            List<Product> productList = ProductVariation.createCriteria().list {
                projections {
                    property("product")
                }
                options {
                    eq("id", id)
                }
            }.unique()
            if(productList.size()) {
                response.product = productList.collect {it.name}
            }
            return response
        })
        AppEventManager.on("after-variation-option-remove", { id ->
            if(!ProductVariation.findAllByIsBase(true)) {
                List<ProductVariation> variations = ProductVariation.createCriteria().list {
                    eq("active", true)
                    eq("product.id", id)
                }
                if(variations) {
                    variations[0].isBase = true
                }
                variations*.save()
            }
        })
        AppEventManager.on("before-product-delete", { id ->
            ProductVariation.createCriteria().list {
                eq("product.id", id)
            }.each {
                AppEventManager.fire("before-variation-delete", [it.details.id])
                it.delete()
            }
        })

        AppEventManager.on("before-variation-option-detach", { id ->
            VariationDetails details = VariationDetails.proxy(id)
            if(details) {
                AppEventManager.fire("before-${details.model}-variation-details-delete", [details.modelId])
                details.modelId = null
                details.save()
            }
        })
        AppEventManager.on("after-variation-delete before-variation-delete", { id ->
            VariationDetails details = VariationDetails.proxy(id)
            if(details) {
                AppEventManager.fire("before-${details.model}-variation-details-delete", [details.modelId])
                details?.delete();
                AppEventManager.fire("after-${details.model}-variation-details-delete", [details.modelId])
            }
        })

        HookManager.register("variation-type-delete-veto") { response, id ->
            def count = VariationOption.createCriteria().count {
                eq("type.id", id)
            }
            if(count) {
                response.options = count
            }
            return response
        }
        HookManager.register("variationType-delete-veto-list", { response, id ->
            List<VariationOption> options = VariationOption.createCriteria().list {
                eq("type.id", id)
            }
            if(options.size()) {
                response.options = options.collect {it.type.name + " : " + it.value}
            }
            return response
        })

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", "variation_type")
            }
            if(contents) {
                VariationType.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        })
    }
    
    static VariationService getInstance() {
        if (!instance) {
            instance = Holders.applicationContext.getBean("variationService")
        }
        return instance
    }

    static {
        AppEventManager.on("variation-update", { id ->
            AppEventManager.fire("product-update", [id])
        })

        AppEventManager.on("order-item-create", { OrderItem orderItem, CartItem cartItem ->
            if(cartItem.object instanceof VariationProductInCart || cartItem.object instanceof DownloadableProductInCart) {
                if(cartItem.object.product.hasProperty('productVariationId')){
                    new OrderVariationItem(variationId: cartItem.object.product.productVariationId, variationModel: cartItem.object.product.variationModel, orderItem: orderItem).save()
                }
            }
        })

        AppEventManager.on("before-order-update", { Long orderId ->
            Order order = Order.get(orderId)
            if(order.items.size()) {
                OrderVariationItem.createCriteria().list {
                    inList("orderItem.id", order.items.id)
                }*.delete()
            }
        });

        HookManager.register("productCartAdd", { response, ProductData data, Product product, Map params ->
            if(PluginManager.isInstalled("variation")) {
                VariationService variationService = VariationService.getInstance()
                if (variationService.allowed() && variationService.hasVariation(product.id) && !params.config && !params.giftRegistryId) {
                    ProductService productService = Holders.applicationContext.getBean("productService")
                    def productData = productService.getProductData(product, [:])
                    params.popupTitle = "please.choose.variation"
                    response.blocks.add([model: [productData: productData]])
                }
            }
            return response
        })

        HookManager.register("variationsForCartAdd variationsForGiftRegistryAdd", { List variationTokens, ProductData data, Map params ->
            def variationId = data.attrs['selectedVariation']
            VariationService variationService = VariationService.getInstance()
            if(variationService.allowed() && variationId) {
                def variation = ProductVariation.get(variationId)
                def comb =  VariationService.getInstance().getVariationTokensForCart(variation)
                variationTokens = variationTokens ? variationTokens + comb : comb
            }
            return variationTokens
        })

        HookManager.register("onMyShoppingXmlGenerate onGetPriceXmlGenerate", { Map response, Product prod, Map config ->
            VariationService variationService = VariationService.getInstance()
            if (variationService.allowed() && config["submit_variations"].toBoolean() && prod.hasVariation()) {
                ProductService productService = ProductService.getInstance()
                List<ProductVariation> variations = ProductVariation.createCriteria().list {
                    eq("product.id", prod.id)
                    eq("active", true)
                }
                variations.each {
                    Map vConf = [:]
                    ProductData productData = vConf.data = productService.getProductData(prod, [variation: it.id])
                    if(productData.description) {
                        List variationList = [];
                        it.options.each { op ->
                            variationList.add(op.type.name + ": " + (op.label ?: op.value))
                        }
                        String variationStr = "(${variationList.join(", ")})";
                        vConf.name = productData.name + variationStr;
                        response.flag = true
                        response.config.push(vConf)
                    }
                }
            }
            return response
        })

        HookManager.register("onGoogleProductXmlGenerate", { Map response, Product product, Map config, Map variationConfig ->
            VariationService variationService = VariationService.getInstance()
            if (variationService.allowed() && config["submit_variations"].toBoolean() && product.hasVariation()) {
                ProductService productService = ProductService.getInstance()
                List<ProductVariation> variations = ProductVariation.createCriteria().list {
                    eq("product.id", product.id)
                    eq("active", true)
                }
                variations.each {
                    Map vConf = [:]
                    ProductData productData = vConf.data = productService.getProductData(product, [variation: it.id])
                    vConf.variationMap = [:]
                    it.options.each { op ->
                        if(variationConfig[op.type.name]) {
                            vConf.variationMap[variationConfig[op.type.name]] = (op.label ?: op.value)
                        }
                    }
                    if (vConf.variationMap.size()) {
                        response.flag = true
                        response.config.push(vConf)
                    }
                }
            }
            return response
        })

        HookManager.register("optionFromVariation", { options, id ->
            VariationService variationService = VariationService.getInstance()
            if(id && variationService.allowed()) {
                return ProductVariation.get(id)?.options.id
            }
        })
    }

    ProductVariation getVariationByOptionList(Product product, List variations) {
        ProductVariation variation
        List values = []
        variations.each {
            def val = it.split(":")
            if(val.size() > 1) {
                values.push(val[1].trim())
            }
        }
        if(values.size()) {
            variation = ProductVariation.findAllByProductAndActive(product, true)?.find {
                List label = it.options.label
                label.size() && label.size() == values.size() && label.intersect(values).size() == values.size()
            }
        }
        return variation
    }

    String getVariationIdentifier(ProductVariation variation) {
        List options = getVariationTokensForCart(variation)
        return options.join(", ")
    }

    List getVariationTokensForCart(ProductVariation variation) {
        List tokens = []
        variation?.options.each {
            tokens.add(it.type.name + ": " + (it.label ?: it.value))
        }
        return tokens
    }

    public ProductVariation getVariationByOptions(Product product, List optionIds = []) {
        ProductVariation variation
        if(optionIds) {
            variation = ProductVariation.lookUpVariation(product.id, optionIds)
        } else {
            def temp = ProductVariation.findAllByProduct(product)
            if(temp) {
                variation = temp.find {it.isBase} ?: (temp.find { it.active } ?: temp.first())
            }
        }
        return variation
    }

    private Closure getCriteriaClosure(Map params) {
        Closure closure = {
            if (params.search) {
                ilike("name", "%${params.search.trim().encodeAsLikeText()}%")
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
        return closure;
    }

    Integer getTypeCount (Map params) {
        return VariationType.createCriteria().get {
            and getCriteriaClosure(params);
            projections {
                rowCount();
            }
        }
    }

    Integer getOptionCount(Map params) {
        return VariationOption.createCriteria().count {
            createAlias("type", "type", JoinType.LEFT_OUTER_JOIN)
            if(params.isDisposable != "true") {
                eq("type.isDisposable", false)
            } else  {
                eq("type.isDisposable", true)
            }
        }
    }

    List<VariationType> getTypes(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return VariationType.createCriteria().list(listMap) {
            and getCriteriaClosure(params);
        }
    }

    List<VariationType> getUsableTypes () {
        return VariationType.list()
    }

    List<VariationType> getSelectedVariationTypes (Long product) {
        return ProductVariation.createCriteria().list {
            eq("product.id", product)
        }.options.type.flatten().unique()
    }

    List<VariationOption> getSelectedVariationOptions (Long product, Long type) {
        List<VariationOption> variationOptions = []
        List options = ProductVariation.createCriteria().list {
            eq("product.id", product)
        }.options
        if(options) {
            variationOptions = options.flatten().unique()
            variationOptions = variationOptions.findAll {it.type.id == type}
        }
        return variationOptions
    }

    List<VariationOption> getOptions (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return VariationOption.createCriteria().list(listMap) {
            createAlias("type", "type", JoinType.LEFT_OUTER_JOIN)
            if(params.isDisposable != "true") {
                eq("type.isDisposable", false)
            } else  {
                eq("type.isDisposable", true)
            }
            order("type")
            order("idx")
        }
    }

    List<VariationOption> getOptionsByType (Long typeId) {
        return VariationOption.createCriteria().list {
            eq "type.id", typeId
        }
    }

    @Transactional
    VariationType saveType(Map params) {
        String name = params.name.trim()
        def count = VariationType.createCriteria().count {
            if(params.id) {
                ne("id", params['id'].toLong())
            }
            eq("name", params.name)
        }
        if(count) {
            throw new ApplicationRuntimeException("variation.type.already.exists", [name])
        }
        VariationType type = params.id ? VariationType.get(params.id) : new VariationType()
        type.name = name
        type.isDisposable = false
        type.standard = params['standard']?.toLowerCase()
        type.save()
        if(type.hasErrors()) {
            return null
        }
        if(params.id) AppEventManager.fire("variation-type-update", [type.id])
        return type
    }

    @Transactional
    Boolean deleteType(Map params, String at2_reply, String at1_reply) {
        Long id = params['id'].toLong()
        TrashUtil.preProcessFinalDelete("variation-type", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-variation-type-delete", [id])
        VariationType type = VariationType.get(id)
        type.delete()
        AppEventManager.fire("variation-type-delete", [id])
        return !type.hasErrors()
    }

    @Transactional
    Boolean deleteValue(Map params, String at2_reply, String at1_reply) {
        Long id = params['id'].toLong()
        TrashUtil.preProcessFinalDelete("variation-option", id, at2_reply != null, at1_reply != null)
        AppEventManager.fire("before-variation-option-delete", [id])
        VariationOption option = VariationOption.get(id)
        option.delete()
        if(option.type.standard == "image") {
            def filePath = AppUtil.session.servletContext.getRealPath("resources/variation/option/option-${option.id}")
            File folder = new File(filePath)
            if (folder.exists()) {
                FileUtils.deleteDirectory(folder)
            }
        }
        AppEventManager.fire("variation-option-delete", [id])
        return !option.hasErrors()
    }

    @Transactional
    def saveOption(TypeConvertingMap params, MultipartFile uploadedFile) {
        def count = VariationOption.createCriteria().count {
            if(params.id) {
                ne "id", params.long("id")
            }
            if(params.label) {
                eq "label", params.label.trim()
            }
            eq "type.id", params.long("type")
            eq "value", params.value.trim()
        }
        if(count) {
            throw new ApplicationRuntimeException("variation.option.already.exists", [params.value], "alert")
        }
        VariationOption option
        def value
        if(params.id) {
            option = VariationOption.get(params.id)
            value = option.value
        } else {
            option = new VariationOption()
        }
        if(params.type) {
            option.type = VariationType.get(params.type)
        }
        option.value = uploadedFile ? uploadedFile.originalFilename : params.value.trim()
        option.label = params.label.trim()
        option.idx = params.int("order")
        option.save()
        Boolean error = option.hasErrors()
        if(!error && uploadedFile) {
            def filePath = PathManager.getResourceRoot("variation/option/option-${option.id}")
            File f = new File(filePath)
            if (!f.exists()) {
                f.mkdirs();
            }
            if(value) {
                File rejectedFile = new File(filePath)
                if(rejectedFile.exists()) {
                    rejectedFile.delete();
                }
            }
            imageService.uploadImage(uploadedFile, NamedConstants.IMAGE_RESIZE_TYPE.VARIATION_IMAGE, option, 5 * 1024, false)
        }
        if(error) {
            return null
        }
        sessionFactory.cache.evictQueryRegions()
        return option
    }

    boolean hasVariation(Long id) {
        return ProductVariation.createCriteria().count {
            eq "product.id", id
        } > 0
    }

    String getVariationModel(Long id) {
        String model = VariationDetails.createCriteria().get {
            projections {
                property "model"
            }
            eq("product.id", id)
            maxResults(1)
        }
        return model ?: null
    }

    @Transactional
    List<ProductVariation> saveVariation(TypeConvertingMap params, Product product = null) {
        String model = params.model
        product = product ?: Product.get(params.pId)
        List types = params.list('variationType')
        List options = []
        types.each {
            def optionIds = params[it].variationOption
            List temp = options
            options = []
            options.addAll(VariationUtils.cartesianProduct(temp, optionIds instanceof String ? [optionIds] : optionIds as List))
        }
        List<ProductVariation> variations = []
        options.eachWithIndex { comb, id ->
            ProductVariation productVariation = new ProductVariation(product: product)
            List combination =  (comb instanceof List || comb instanceof Object[]) ? comb : [comb]
            combination.each {
                productVariation.addToOptions(VariationOption.get(it))
            }
            productVariation.details = new VariationDetails(model: model, product: product).save()
            productVariation.save()
            AppEventManager.fire("after-${model}-variation-create", [productVariation.details])
            if(!productVariation.hasErrors()) {
                variations.add(productVariation)
            }
        }
        return variations
    }

    @Transactional
    Boolean addOption(Map params) {
        Product product = Product.get(params.product)
        VariationOption option = VariationOption.get(params.option)
        List<ProductVariation> variations = ProductVariation.findAllByProduct(product)
        Integer count = 0
        if(params.type) {
            variations.each { variation ->
                if(!variation.options.contains(option)) {
                    AppEventManager.fire("before-variation-option-detach", [variation.details.id])
                    variation.addToOptions(option)
                    variation.active = false
                    variation.isBase = false
                    variation.save()
                    if(!variation.hasErrors()) {
                        count++
                    }
                    AppEventManager.fire("after-${params.model}-variation-option-attach", [variation.details])
                }
            }
        } else {
            VariationType type = option.type
            List optionList = []
            variations.options.each { options ->
                List temp = options.findAll {it.type != type}
                temp.add(option)
                if(temp && !optionList.contains(temp)) {
                    optionList.push(temp)
                }
            }
            optionList.each { options ->
                if(!variations.find {it.options == options}) {
                    ProductVariation productVariation = new ProductVariation(product: product)
                    productVariation.options = options
                    productVariation.details = new VariationDetails(model: params.model, product: product).save()
                    productVariation.save()
                    if(!productVariation.hasErrors()) {
                        count++
                    }
                    AppEventManager.fire("after-${params.model}-variation-option-attach", [productVariation.details])
                }
            }
        }
        if(count) {
            AppEventManager.fire("variation-update", [product.id])
        }
        return count > 0
    }

    @Transactional
    Boolean removeOption(Map params) {
        Product product = Product.get(params.product)
        VariationOption option = VariationOption.get(params.option)
        Integer count = 0
        List<ProductVariation> variations = ProductVariation.createCriteria().list {
            eq("product.id", product.id)
            options {
                eq("id", option.id)
            }
        }
        variations.each {
            if(it.options.size() > 1) {
                AppEventManager.fire("before-variation-option-detach", [it.details.id])
                it.options.remove(option)
                it.active = false
                it.isBase = false
                it.save()
                AppEventManager.fire("after-${it.details.model}-variation-option-detach", [it.details])
            } else {
                it.delete()
                AppEventManager.fire("after-variation-delete", [it.details.id])
            }
            count++
        }
        AppEventManager.fire("after-variation-option-remove", [product.id])
        AppEventManager.fire("variation-update", [product.id])
        return count > 0
    }

    @Transactional
    Boolean activateVariation(Map params) {
        ProductVariation variation = ProductVariation.get(params.id)
        if(params.deactivate) {
            variation.active = false
        } else {
            variation.active = true
            if (variation.product.isInventoryEnabled){
                variation.product.isInventoryEnabled = false
            }
        }
        variation.save(flush: true)
        if(variation.active) {
            AppEventManager.fire(variation.details.model + "-variation-activate", [variation.details])
        }
        AppEventManager.fire("variation-update", [variation.product.id])
        return !variation.hasErrors()
    }

    @Transactional
    Boolean setDefault(Map params) {
        List<ProductVariation> variations = ProductVariation.findAllByProduct(Product.get(params.pId));
        variations.each {
            it.isBase = false
        }
        variations*.save()
        ProductVariation variation = ProductVariation.get(params.id)
        variation.isBase = true
        variation.save()
        AppEventManager.fire("variation-update", [variation.product.id])
        return !variation.hasErrors()
    }

    Map getDataForProductInfoAPI(Product product) {
        Map apiData = [:]
        List<ProductVariation> productVariations = ProductVariation.createCriteria().list {
            eq "product.id", product.id
        }
        if(productVariations.size() == 0) {
            return null
        }
        apiData["variationModel"] = productVariations[0].details.model
        List<VariationOption> options = productVariations.options.flatten().unique()
        List<VariationType> types = options.type.flatten().unique()
        String nonRequestBaseUrl = app.nonRequestBaseUrl()
        apiData["types"] = types.collect { type ->
            List<VariationOption> optionsByType = options.findAll { it.type == type }
            [
                    id: type.id,
                    name: type.name,
                    standard: type.standard,
                    options: optionsByType.collect {
                        [
                                id: it.id,
                                label: it.label,
                                value: type.standard ==  DC.VARIATION_REPRESENTATION.IMAGE ? "${nonRequestBaseUrl}resources/variation/option/option-${it.id}/16-${it.value}" : it.value
                        ]
                    }
            ]
        }
        apiData["availableCombinations"] = productVariations
        return apiData
    }

    Boolean isAvailable(ProductData data) {
        Boolean available = data.isAvailable
        boolean considerStock = data.isInventoryEnabled && AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE,
                "order_quantity_over_stock") != DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY
        if(considerStock && data.availableStock < 1) {
            available = false
        }
        return available
    }

    Boolean allowed() {
        if(LicenseManager.isAllowed("allow_variation_feature")) {
            return true
        }
        return false
    }

    Boolean allowedStandard() {
        if(allowed() && LicenseManager.isAllowed("allow_standard_variation_feature")) {
            return true
        }
        return false
    }

    Boolean allowedEnterprise() {
        if(allowed() && LicenseManager.isAllowed("allow_enterprise_variation_feature")) {
            return true
        }
        return false
    }

    Boolean allowed(Long productId) {
        String model = getVariationModel(productId)
        if(model == null) {
            return true
        } else if(model == "standard") {
            return allowedStandard()
        } else {
            return allowedEnterprise()
        }
    }

    Boolean isVariationExist(Long productId) {
        return ProductVariation.where {
            eq("product.id", productId)
        }.count() > 0
    }
}
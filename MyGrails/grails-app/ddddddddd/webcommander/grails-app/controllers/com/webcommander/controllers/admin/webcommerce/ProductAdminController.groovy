package com.webcommander.controllers.admin.webcommerce

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.common.FileService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.license.blocker.ProductLicense
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.ProductData
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.springframework.web.multipart.MultipartFile

class ProductAdminController {

    CommonService commonService
    ProductService productService
    ImageService imageService
    FileService fileService
    TaxService taxService

    def editProductPage() {
        List favoriteWidgets = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "favorite_product_widgets")?.split(",") ?: []
        List widgets = new ArrayList<>(DomainConstants.PRODUCT_WIDGET_TYPE.values())
        for (String w : favoriteWidgets) {
            widgets.remove(w)
        }
        Long id = params.long("id")
        def editorUrl = app.relativeBaseUrl() + "productAdmin/renderEditor?id=" + id + "&editMode=true"
        render(view: "/admin/contentEditor/editContent", model: [id: id, isAutoPage: true, editorUrl: editorUrl, widgets: widgets, favoriteWidgets: favoriteWidgets, noResponsive: true, widgetLabels: NamedConstants.PRODUCT_WIDGET_MESSAGE_KEYS, widgetLicense: LicenseConstants.PRODUCT_WIDGET])
    }

    @Restriction(permission = "product.edit.properties", entity_param = "id", domain = Product, owner_field = "createdBy")
    def loadProductEditor() {
        Long productId = params.id ? params.long("id") : null
        Product product = productService.getProduct(productId)
        render(view: "/admin/item/product/editor", model: [productId: productId, product: product])
    }

    def loadProductBulkEditor() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        List<Long> ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        render(view: "/admin/item/product/bulkEdit/bulkEditor", model: [productIds: ids])
    }

    def loadAvailableImage() {
        Long id = params.id ? params.id.toLong(0) : 0
        Product product = Product.findById(id)
        render(view: "/admin/item/product/variation/imageSelector", model: [product: product])
    }

    def loadAvailableImageAsJSON() {
        Long id = params.id ? params.id.toLong(0) : 0
        Product product = Product.findById(id)
        Map productImages = [:]
        product?.images?.each {
            productImages[it.id] = it.name
        }
        render(productImages as JSON)
    }

    @Restrictions([
        @Restriction(permission = "product.create", params_not_exist = "id"),
        @Restriction(permission = "product.edit.properties", params_exist = "id", entity_param = "id", domain = Product, owner_field = "createdBy")
    ])
    @License(required = "product_limit", checker = ProductLicense)
    def saveBasicProperties() {
        if (params.deleteTrashItem) {
            def field = params.deleteTrashItem.collect { it.key }[0]
            def value = params[field]
            productService.deleteTrashItemAndSaveCurrent(field, value)
        }
        def result = productService.saveBasics(params)
        if (result) {
            render([status: "success", message: g.message(code: "product.save.success"), id: result.id, name: params.name] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveBasicBulkProperties() {
        def save = productService.saveBasicBulkProperties(params)
        if(save) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def savePriceStockBulkProperties() {
        def save = productService.savePriceStockBulkProperties(params)
        if(save) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveAdvancedBulkProperties() {
        def save = productService.saveAdvancedBulkProperties(params)
        if(save) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveSeoBulkProperties() {
        def save = productService.saveSeoBulkProperties(params)
        if(save) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def savePriceNQuantityProperties() {
        Map result = productService.savePriceNQuantity(params)
        if (result.status) {
            render([status: "success", message: g.message(code: "product.save.success"), availableStock: result.availableStock] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveAdvanced() {
        if (productService.saveAdvanced(params)) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveRelated() {
        if (productService.saveRelated(params)) {
            render([status: "success", message: g.message(code: "product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def saveIncluded(){
        if (productService.saveIncluded(params)) {
            render([status: "success", message: g.message(code: "included.product.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "included.product.save.failure")] as JSON)
        }
    }

    def updateImages() {
        boolean success
        if((success = productService.updateImages(params))) {
            def images = request.getMultiFileMap().images
            if(images && images.size()) {
                Product product = Product.get(params.id)
                success = productService.saveImages(product, images)
            }
        }
        if(success) {
            render([status: "success", message: g.message(code: "product.images.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.images.can.not.save")] as JSON)
        }
    }

    def updateVideos() {
        boolean success
        if ((success = productService.updateVideos(params))) {
            def videos = request.getMultiFileMap().videos
            if(videos && videos.size()) {
                Product product = Product.get(params.id)
                success = productService.saveVideos(product, videos)
            }
        }
        if(success) {
            render([status: "success", message: g.message(code: "product.videos.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.videos.could.not.save")] as JSON)
        }
    }

    def editImage() {
        ProductImage image = ProductImage.get(params.id)
        String imageAltTag = params.altText
        render(view: "/admin/item/product/imageProperty", model: [image: image, imageAltTag: imageAltTag])
    }

    def updateImageProperties() {
        if (productService.updateImageProperty(params)) {
            render([status: "success", message: g.message(code: "product.image.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.image.could.not.save")] as JSON)
        }
    }

    @Restrictions([
        @Restriction(permission = "product.create", params_not_exist = "id"),
        @Restriction(permission = "product.edit.price.stock", params_match_key = "property", params_match_value = "price-stock", entity_param = "id", domain = Product, owner_field = "createdBy"),
        @Restriction(permission = "product.edit.properties", params_match_key = "property", params_match_value = "basic", entity_param = "id", domain = Product, owner_field = "createdBy")
    ])
    @License(required = "product_limit", checker = ProductLicense)
    def loadProductProperties() {
        Product product = params.id ? productService.getProductReadonly(params.long("id")) : new Product(sku: commonService.getSKUForDomain(Product))
        def parentCategory = 0
        if(params.categoryId) {
            parentCategory = params.long("categoryId")
        }
        switch (params.property) {
            case "basic":
                render(view: "/admin/item/product/basic", model: [product: product, parentCategory: parentCategory])
                break
            case "price-stock":
                def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
                boolean showTaxProfile = true
                String taxConfigurationType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type")
                if (taxConfigurationType && taxConfigurationType.equals(DomainConstants.TAX_CONFIGURATION_TYPE.DEFAULT)) {
                    showTaxProfile = false
                }
                params.isDefault = false
                def inventoryManipulation = [product:product, isInventoryEnable: true]
                inventoryManipulation = HookManager.hook("productInventoryManipulation", inventoryManipulation, inventoryManipulation)
                List<ShippingProfile> profiles = taxService.getTaxProfiles(params)
                render(view: "/admin/item/product/priceAndQuantity", model: [
                        product: inventoryManipulation.product,
                        unitLength: generalSettings.unit_length,
                        unitWeight: generalSettings.unit_weight,
                        showTaxProfile: showTaxProfile,
                        profiles: profiles,
                        isInventoryEnable: inventoryManipulation.isInventoryEnable
                ])
                break
            case "image-video":
                render(view: "/admin/item/product/imageAndVideo", model: [product: product])
                break
            case "advanced":
                render(view: "/admin/item/product/advanced", model: [product: product])
                break
            case "relatedProducts":
                render(view: "/admin/item/product/related", model: [product: product, products: product.relatedProducts])
                break
            case "includedProducts":
                def includedProducts = params.id ? productService.getIncludedProducts(params) : []
                render(view: "/admin/item/product/included", model: [product: product, products: includedProducts])
                break
            case "productFile":
                render(view: "/admin/item/product/productFile", model: [product: product])
                break
        }
    }

    def loadProductBulkProperties() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        params.ids = jsonSlurper.parseText(params.ids).collect{it.toLong()}
        List<Product> products = params.ids ? Product.findAllByIdInList(params.ids) : []
        Integer count = products.size()
        switch (params.property) {
            case "basic":
                render(view: "/admin/item/product/bulkEdit/basic", model: [products: products, count: count])
                break
            case "price-stock":
                String tax = productService.getCustomDomainList(TaxProfile) as JSON
                String shipping = productService.getCustomDomainList(ShippingProfile) as JSON
                render(view: "/admin/item/product/bulkEdit/priceAndStock", model: [products: products, count: count, tax: tax, shipping: shipping])
                break
            case "advanced":
                render(view: "/admin/item/product/bulkEdit/advanced", model: [products: products, count: count])
                break
            case "webtool":
                render(view: "/admin/item/product/bulkEdit/webtool", model: [products: products, count: count])
                break
            case "parent-dom":
                render(view: "/admin/item/product/bulkEdit/parentSelector", model: [products: products])
                break
        }
    }

    def loadChangeAllOnSale() {
        render(view: "/admin/item/product/bulkEdit/changeOnSalePrice", model: [])
    }

    def productSelectionPopup() {
        List<Long> ids = params.list("product").collect { it.toLong(0) }
        List<Product> products = ids ? Product.createCriteria().list {
            inList("id", ids)
        } : []
        params.fieldName = params.fieldName ?: "products"
        render(view: "/admin/item/product/productSelectionPopup", model: [products: products, params: params])
    }

    @Restriction(permission = "product.view.list")
    def loadProductsForSelection() {
        params.max = params.max ?: "10"
        if(!params.containsKey('parent')) {
            params.parent = "all"
        }
        Integer count = productService.getProductsCount(params)
        List<Product> products = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            productService.getProducts(params)
        }
        render(view: "/admin/item/product/selectionPanel", model: [count: count, products: products, params: params])
    }

    @Restriction(permission = "product.create")
    @License(required = "product_limit", checker = ProductLicense)
    def copyProduct() {
        if (productService.copyProduct(params.long("id"))) {
            render([status: "success", message: g.message(code: "product.copy.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.copy.failure")] as JSON)
        }
    }

    @Restriction(permission = "product.remove", entity_param = "id", domain = Product, owner_field = "createdBy")
    def deleteProduct() {
        try {
            boolean deleteSuccess
            Product product = Product.proxy(params.id.toLong(0))
            if(product) {
                deleteSuccess = productService.putProductInTrash(params.long("id"), params.at2_reply, params.at1_reply)
            }
            if (deleteSuccess) {
                render([status: "success", message: g.message(code: "product.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "product.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @Restriction(permission = "product.remove", entity_param = "ids", domain = Product, owner_field = "createdBy")
    def deleteSelected() {
        if (productService.putSelectedProductsInTrash(params.list("ids").collect{it.toLong()})) {
            render([status: "success", message: g.message(code: "selected.products.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "selected.products.could.not.delete")] as JSON)
        }
    }

    def adjustInventory() {
        Long id = params.long("id")
        Integer changeQuantity = params.int("changeQuantity")
        String note = params.note
        Map result = productService.adjustInventory(id, changeQuantity, note)
        if (result.success) {
            render([status: "success", message: g.message(code: "product.save.success"), availableStock: result.availableStock] as JSON)
        } else {
            render([status: "error", message: g.message(code: "product.could.not.save")] as JSON)
        }
    }

    def changeOrder() {
        long id = params.long("id")
        int value = params.int("value")
        productService.changeOrder(id, value)
        render([status: "success"] as JSON)
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Product, params.long("id"), params.field, params.value) as JSON)
    }

    def restoreFromTrash() {
        String sku = params.value
        Long id = productService.restoreProductFromTrash(sku)
        render([status: "success", message: g.message(code: "restored.successfully", args: ["Product"]), type: "product", id: id] as JSON)
    }

    def saveCurrentOrder() {
        if (productService.saveCurrentOrder(params)) {
            render([status: "success", message: g.message(code: "order.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "order.save.failure")] as JSON)
        }
    }

    def view() {
        Product product = Product.get(params.id)
        render(view: "/admin/item/product/view", model: [product: product])
    }

    def inventoryHistory() {
        params.max = "5"
        params.offset = params.offset ?: "0"
        Integer count = productService.getInventoryHistoryCount(params)
        List<ProductInventoryAdjustment> histories = productService.getInventoryHistory(params)
        render(view: "/admin/item/product/inventoryHistory", model: [histories: histories, count: count, max: params.max, offset: params.offset])
    }

    def renderEditor() {
        def eCommerceConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)
        render (
                view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PRODUCT_PAGE,
                                                    editMode: true, config: eCommerceConfigs, macros: [:]]
        )
    }

    def updateSpec() {
        if(!params["remove_spec"] && params.productSpec == "") {
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
            return
        }
        boolean success
        MultipartFile specFile = params.remove_spec == "true" ? null : request?.getFile("productSpec")
        Product product = Product.get(params["id"].toLong())
        success = productService.specUpload(product, specFile)
        if (success) {
            AppEventManager.fire("product-update", [product.id])
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "resource.update.failure")] as JSON)
        }
    }

    def updateFile() {
        MultipartFile productFile = request.getFile("productFile")
        def result = productService.updateProductFile(params, productFile)
        if (result) {
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "resource.update.failure")] as JSON)
        }
    }

    def downloadProductFile() {
        Product product = Product.get(params.id)
        def productFileName
        if(params.isVariationFile){
            productFileName = HookManager.hook("get-virtual-product-variation-data",params).name
        }else{
            productFileName = product.productFile.name
        }
        String productFileRelativePath = appResource.getDownloadableProductTypeFileUrl(productId: params.id, productFileName: productFileName, isVariationFile: params.isVariationFile)
        InputStream inputStream = fileService.readModifiableResourceFromSystem(productFileRelativePath, NamedConstants.CLOUD_CONFIG.DEFAULT)
        if(inputStream) {
            response.setHeader("Content-disposition", "attachment filename=\"${productFileName}\"")
            response.outputStream <<inputStream
        } else {
            render text:  g.message(code: "file.not.available")
        }
    }

    def loadProductSelector() {
        List products = []
        if(params.category == "root") {
            products = Product.findAllByParentAndIsInTrash(null, false)
        } else if(params.category == "") {
            products = Product.findAllByIsInTrash(false)
        } else {
            products = Product.findAllByParentAndIsInTrash(Category.get(params['category']), false)
        }
        render(view: "/admin/item/product/productSelector", model: [products: products])
    }
}

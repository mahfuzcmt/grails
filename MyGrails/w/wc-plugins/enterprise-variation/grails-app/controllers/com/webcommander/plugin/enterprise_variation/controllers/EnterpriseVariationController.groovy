package com.webcommander.plugin.enterprise_variation.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.enterprise_variation.EnterpriseVariationService
import com.webcommander.plugin.enterprise_variation.EvariationDetails
import com.webcommander.plugin.enterprise_variation.VariationInventoryHistory
import com.webcommander.plugin.enterprise_variation.VariationProductImage
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile

class EnterpriseVariationController {
    VariationService variationService
    EnterpriseVariationService enterpriseVariationService

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def loadProductEditor() {
        ProductVariation variation = ProductVariation.get(params.vId)
        Product product = Product.get(variation.productId)
        render(view: "/plugins/enterprise_variation/admin/productEditor", model: [variation: variation,  product: product])
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def loadProductProperties() {
        ProductVariation variation = ProductVariation.get(params.vId)
        Product product = variation.product
        Map details = [:]
        EvariationDetails variationDetails = variation.details.modelId ? EvariationDetails.get(variation.details.modelId) : new EvariationDetails()
        variationDetails.options.findAll {it.field.startsWith(params.property + ".")}.each {
            String key = it.field
            details[key.substring(key.indexOf('.')+1)] = it.value
            if(it.description) {
                details["description"] = it.description.content
            }
        }
        details.id = variationDetails.id
        switch (params.property) {
            case "basic":
                details.putAll([name: variationDetails.name, sku: variationDetails.sku, url: variationDetails.url])
                render(view: "/plugins/enterprise_variation/admin/product_editor_tabs/basic", model: [product: product, variation: variation, detailsMap: details, details: variationDetails])
                break;
            case "priceStock":
                def generalSettings = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL)
                render(view: "/plugins/enterprise_variation/admin/product_editor_tabs/priceStock", model: [product: product, unitLength: generalSettings.unit_length,
                    unitWeight: generalSettings.unit_weight, detailsMap: details, details: variationDetails, variationId: variation.id])
                break;
            case "imageVideo":
                render(view: "/plugins/enterprise_variation/admin/product_editor_tabs/imageVideo", model: [product: product, detailsMap: details, details: variationDetails])
                break;
            case "advanced":
                render(view: "/plugins/enterprise_variation/admin/product_editor_tabs/advanced", model: [product: product, detailsMap: details, details: variationDetails, variationId: variation.id])
                break
            case "productFile":
                render(view: "/plugins/enterprise_variation/admin/product_editor_tabs/productFile", model: [product: product, detailsMap: details, details: variationDetails])
                break
        }
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def saveProperties() {
        Boolean success = enterpriseVariationService."save${params['type'].capitalize()}"(params)
        if(success) {
            VariationDetails details = VariationDetails.findByModelId(params['id'].toLong())
            AppEventManager.fire("variation-update", [details.product.id])
            render ([status: "success", message: g.message(code: "variation.save.success")] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "variation.save.failed")] as JSON)
        }
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def updateSpec() {
        if(!params["remove_spec"] && params.productSpec == "") {
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
            return
        }
        boolean success
        MultipartFile specFile = params.remove_spec? null : request?.getFile("productSpec")
        EvariationDetails eDetails = EvariationDetails.get(params.id)
        success = enterpriseVariationService.specUpload(eDetails, specFile)
        if (success) {
            VariationDetails details = VariationDetails.findByModelId(eDetails.id)
            AppEventManager.fire("variation-update", [details.product.id])
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "resource.update.failure")] as JSON)
        }
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def updateFile() {
        MultipartFile productFile = request.getFile("productFile")
        EvariationDetails eDetails = EvariationDetails.get(params.id)
        def result = enterpriseVariationService.updateProductFile(params, productFile)
        if (result) {
            VariationDetails details = VariationDetails.findByModelId(eDetails.id)
            AppEventManager.fire("variation-update", [details.product.id])
            render([status: "success", message: g.message(code: "resource.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "resource.update.failure")] as JSON)
        }
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def inventoryHistory() {
        params.max = "5";
        params.offset = params.offset ?: "0"
        Integer count = enterpriseVariationService.getInventoryHistoryCount(params)
        List<VariationInventoryHistory> histories = enterpriseVariationService.getInventoryHistory(params)
        render(view: "/admin/item/product/inventoryHistory", model: [histories: histories, count: count, max: params.max, offset: params.offset])
    }

    @License(required = "allow_enterprise_variation_feature")
    @RequiresAdmin
    def editImage() {
        VariationProductImage image = VariationProductImage.get(params.id)
        render(view: "/plugins/enterprise_variation/admin/imageProperty", model: [image: image, imageAltTag: params.altText]);
    }

    def loadVariationProductUrl() {
        Product product = Product.get(params.productId)
        Map config = params['config']
        List options = config.options instanceof String ? [config.options] : config.options as List
        ProductVariation variation = config.variation ? ProductVariation.get(config.variation) : variationService.getVariationByOptions(product, options);
        if(variation && variation.details.modelId) {
            EvariationDetails details = EvariationDetails.get(variation.details.modelId)
            render([status: "success", url: details?.url] as JSON)
        } else {
            render(status: "error", message: g.message(code: "product.not.available"))
        }
    }

}

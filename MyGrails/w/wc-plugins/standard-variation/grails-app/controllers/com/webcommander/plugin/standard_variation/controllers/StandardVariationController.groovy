package com.webcommander.plugin.standard_variation.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.models.ProductData
import com.webcommander.plugin.standard_variation.StandardVariationService
import com.webcommander.plugin.standard_variation.SvariationDetails
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class StandardVariationController {
    ProductService productService
    StandardVariationService standardVariationService

    @License(required = "allow_standard_variation_feature")
    @RequiresAdmin
    def loadCombinationSetting() {
        ProductVariation variation = ProductVariation.get(params.id)
        SvariationDetails details = SvariationDetails.get(variation.details.modelId ?: 0)
        details = details ?: new SvariationDetails()
        render(view: "/plugins/standard_variation/admin/config", model: [variation: variation, sDetails: details]);
    }

    @License(required = "allow_standard_variation_feature")
    @RequiresAdmin
    def updateCombination() {
        Boolean save = standardVariationService.updateCombination(params)
        if(save) {
            render ([status: "success"] as JSON)
        } else {
            render ([status: "error", message: g.message(code: "settings.update.failed")] as JSON)
        }
    }

    def loadImageWidget() {
        Product product = Product.proxy(params.productId)
        try {
            Map config = params.config ?: [:]
            ProductData data = productService.getProductData(product, config)
            request.product = product
            request.productData = data
            Map model = [status: "success", imageWidget: wi.productwidget(type: "productImage", product: product)]
            render(model as JSON)
        } catch(ApplicationRuntimeException t) {
            render([status: "error", message: ""] as JSON)
        }
    }

    def loadAvailableImage() {
        Long id = params.id ? params.id.toLong(0) : 0
        Product product = Product.findById(id)
        render(view: "/plugins/standard_variation/admin/imageSelector", model: [product: product])
    }

    def loadAvailableImageAsJSON() {
        Long id = params.id ? params.id.toLong(0) : 0
        Product product = Product.findById(id)
        Map productImages = [:]
        product?.images?.each {
            productImages[it.id] = appResource.getProductImageURL(image: it, size: "150")
        }
        render(productImages as JSON)
    }

}

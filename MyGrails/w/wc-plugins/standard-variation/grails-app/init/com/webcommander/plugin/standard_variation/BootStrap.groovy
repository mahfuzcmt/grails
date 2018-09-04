package com.webcommander.plugin.standard_variation

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.plugin.standard_variation.constant.DomainConstants
import com.webcommander.plugin.standard_variation.factory.StandardVariationObjectsFactory
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.factory.VariationObjectsProducer
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductImage
import grails.util.Holders
import com.webcommander.plugin.variation.constant.DomainConstants as DC

class BootStrap {

    List domain_constants = [
            [constant: "PRODUCT_EXPORT_MANDATORY_FIELDS", key: "standardVariation", value: "standardVariation"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "standard_variation", value: true],
    ]

    List named_constants = [
            [constant: "PRODUCT_IMPORT_EXTRA_FIELDS", key: "standardVariation", value: "standard.variation"]
    ]

    List variation_constant = [
            [constant: "VARIATION_MODELS", key: "standard", value: [label: "standard.variation", license: "allow_standard_variation_feature"]]
    ]

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            com.webcommander.constants.DomainConstants.removeConstant(domain_constants)
            NamedConstants.removeConstant(named_constants)
            DC.removeConstant(variation_constant)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin Standerd Variation From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def tenantInit = { tenant ->
        com.webcommander.constants.DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        DC.addConstant(variation_constant)
    }

    def init = { servletContext ->

        VariationObjectsProducer.setVariationObjectsFactory("standard", new StandardVariationObjectsFactory())
        VariationObjectsProducer.setVariationServiceBean("standard", Holders.grailsApplication.mainContext.getBean(StandardVariationService))

        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("standard-variation-details-for-api", { Map data, ProductVariation variation  ->
            VariationDetails details = variation.details
            SvariationDetails sDetails = SvariationDetails.get(details.modelId)
            if (sDetails) {
                Product product = details.product
                data.basePrice = product.basePrice
                switch (sDetails.priceAdjustableType) {
                    case DomainConstants.PRICE_ADJUSTABLE_TYPE.FIXED :
                        data.basePrice = sDetails.price
                        break
                    case DomainConstants.PRICE_ADJUSTABLE_TYPE.ADD:
                        data.basePrice = product.basePrice + sDetails.price
                        break
                    case DomainConstants.PRICE_ADJUSTABLE_TYPE.REDUCE :
                        data.basePrice = product.basePrice - sDetails.price
                        break
                }
                ProductImage image = sDetails.imageId ? ProductImage.get(sDetails.imageId) : null
                if(image) {
                    Map imageMap = [:]
                    imageMap["id"] = image.id
                    imageMap["thumbnail"] =  app.baseUrl() + "resources/product/product-" + product.id + "/150-" + image.name;
                    imageMap["url"] = app.baseUrl() + "resources/product/product-" + product.id + "/" + image.name;
                    data.image = imageMap
                }
            }
            return data;
        })
    }
}
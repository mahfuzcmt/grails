package com.webcommander.plugin.enterprise_variation

import com.webcommander.AppResourceTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.plugin.enterprise_variation.factory.EnterpriseVariationObjectsFactory
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.constant.DomainConstants
import com.webcommander.plugin.variation.factory.VariationObjectsProducer
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

class BootStrap {

    EnterpriseVariationService enterpriseVariationService

    List variation_constant = [
            [constant: "VARIATION_MODELS", key: "enterprise", value: [label: "enterprise.variation", license: "allow_enterprise_variation_feature"]]
    ]

    List domain_constants = [
            [constant: "PRODUCT_EXPORT_MANDATORY_FIELDS", key: "enterpriseVariation", value: "enterpriseVariation"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "enterprise_variation", value: true],
    ]

    List named_constants = [
            [constant: "PRODUCT_IMPORT_EXTRA_FIELDS", key: "enterpriseVariation", value: "enterprise.variation"]
    ]


    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            com.webcommander.constants.DomainConstants.removeConstant(domain_constants)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(variation_constant)
            List metaTags = util.executeQuery("SELECT meta_tag_id FROM evariation_details_meta_tag").collect {
                it.meta_tag_id
            };
            util.executeStatement("DELETE FROM product_variation_variation_option WHERE product_variation_options_id IN(SELECT id FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='enterprise'))")
                    .executeStatement("DELETE FROM product_variation WHERE details_id IN(SELECT id FROM variation_details WHERE model='enterprise')")
                    .executeStatement("DELETE FROM variation_details where model='enterprise'")
                    .deleteResourceFolders("variation/product");
            if (metaTags) {
                util.executeStatement("DELETE FROM meta_tag WHERE id IN(${metaTags.join(",")})")
            }
        } catch (Exception e) {
            log.error "Could Not Uninstall Plugin Enterprise Variation From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def tenantInit = { tenant ->
        com.webcommander.constants.DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        DomainConstants.addConstant(variation_constant)
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(AppResourceTagLib).metaClass.mixin EnterpriseVariationResourceTagLib
        VariationObjectsProducer.setVariationObjectsFactory("enterprise", new EnterpriseVariationObjectsFactory())
        VariationObjectsProducer.setVariationServiceBean("enterprise", enterpriseVariationService)

        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("enterprise-variation-details-for-api", { Map data, ProductVariation variation  ->
            return enterpriseVariationService.getDataForAPI(data, variation)
        })

        HookManager.register("product-data-adjustment", { ProductData data, params ->
            def vId = params.get("config")?.get("variation") ?: (data.hasProperty('productVariationId') ? data.productVariationId : null)
            if (vId) {
                ProductVariation variation = ProductVariation.get(vId)
                if (variation.details.modelId) {
                    EvariationDetails variationDetails = EvariationDetails.get(variation.details.modelId)
                    data.populateSpecInfo(variationDetails.spec, variationDetails.id)
                }
            }
            return data
        })

        HookManager.register("virtual-product-data-adjustment", { ProductData data, params ->
            def vId = params.get("config")?.get("variation") ?: (data.hasProperty('productVariationId') ? data.productVariationId : null)
            if (vId) {
                ProductVariation variation = ProductVariation.get(vId)
                if (variation.details.modelId) {
                    EvariationDetails variationDetails = EvariationDetails.get(variation.details.modelId)
                    data.productFile = variationDetails?.productFile?.name
                    data.productVariationId = variationDetails.id
                    data.populateSpecInfo(variationDetails.spec, variationDetails.id)
                }
            }
            return data
        })

        HookManager.register("get-virtual-product-variation-data", { params ->
            EvariationDetails variationDetails  = EvariationDetails.get(params.id)
            return variationDetails.productFile
        })
    }
}
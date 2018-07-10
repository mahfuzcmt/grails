package com.webcommander.plugin.product_custom_fields

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.manager.HookManager
import com.webcommander.models.ProductData
import com.webcommander.plugin.product_custom_fields.mixin_service.ProductWidgetService as PWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Product
import grails.util.Holders

class BootStrap {

    private final String PRODUCT_CUSTOM_FIELD = "customField"

    List domain_constants = [
            [constant: "PRODUCT_WIDGET_TYPE", key: "CUSTOME_FIELD", value: PRODUCT_CUSTOM_FIELD],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "product_custom_fields", value: true],
    ]

    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: PRODUCT_CUSTOM_FIELD + ".title", value: "custom.field.widget"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: PRODUCT_CUSTOM_FIELD + ".label", value: "custom.field"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()

        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, PRODUCT_CUSTOM_FIELD)
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin product-review From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

        DomainConstants.removeConstant(domain_constants)
        NamedConstants.removeConstant(named_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin PWS
        HookManager.register("productCartAdd", { response, ProductData productData, Product product, params ->
            def service = AppUtil.getBean(ProductCustomFieldService)
            boolean requiresCustom = false
            if (service.getFieldsCount(product) && !params.hasCustomField) {
                requiresCustom = true
            }
            if (requiresCustom) {
                response.blocks.add([label: "choose.required.options", requiresKey: "requiresCustomField"])
            }
            return response
        })
    }
}

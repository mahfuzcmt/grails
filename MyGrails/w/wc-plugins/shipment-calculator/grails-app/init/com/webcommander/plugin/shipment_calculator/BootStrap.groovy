package com.webcommander.plugin.shipment_calculator

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.manager.HookManager
import com.webcommander.plugin.shipment_calculator.mixin_service.ProductWidgetService as SCWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

class BootStrap {

    def config_type = DomainConstants.SITE_CONFIG_TYPES.SHIPPING
    private final String SHIPMENT_CALCULATOR = "shipmentCalculator"

    List domain_constants = [
            [constant:"PRODUCT_WIDGET_TYPE", key: "SHIPMENT_CALCULATOR", value: SHIPMENT_CALCULATOR],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "shipment_calculator", value: true],
    ]

    List named_constants = [
            [constant:"PRODUCT_WIDGET_MESSAGE_KEYS", key: SHIPMENT_CALCULATOR + ".title", value:"shipment.calculator.widget"],
            [constant:"PRODUCT_WIDGET_MESSAGE_KEYS", key: SHIPMENT_CALCULATOR + ".label", value:"shipment.calculator"]
    ]

    Map config = [
            shipment_calculator_cart_details_page : "true",
            shipment_calculator_checkout_page : "false",
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        config.each {
            if (!SiteConfig.findAllByConfigKeyAndType(it.key, DomainConstants.SITE_CONFIG_TYPES.SHIPPING)) {
                new SiteConfig(type: config_type, configKey: it.key, value: it.value ).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, SHIPMENT_CALCULATOR)
            util.removeSiteConfig(config_type, config.keySet())
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin blog From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin SCWS

        SiteConfig.INITIAL_DATA[config_type] += config;
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("auto-page-js", { scripts, pageName ->
            if(pageName == "checkout") {
                scripts.add("plugins/shipment-calculator/js/shipment-calculator.js")
            }
        })
    }
}

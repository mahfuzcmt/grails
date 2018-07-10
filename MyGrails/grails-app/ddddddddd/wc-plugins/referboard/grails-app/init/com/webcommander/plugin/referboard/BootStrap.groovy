package com.webcommander.plugin.referboard

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.ProductWidgetService
import com.webcommander.plugin.referboard.mixin_service.ProductWidgetService as RPWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

class BootStrap {

    private final String REFERBOARD = "referboard"

    List domain_constants = [
            [constant: "PRODUCT_WIDGET_TYPE", key: "REFERBOARD", value: REFERBOARD],
            [constant: "SITE_CONFIG_TYPES", key: "REFERBOARD", value: REFERBOARD],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "referboard", value: true],
    ]
    List named_constants = [
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: REFERBOARD + ".title", value: "referboard.widget"],
            [constant: "PRODUCT_WIDGET_MESSAGE_KEYS", key: REFERBOARD + ".label", value: "referboard"],
    ]
    List license_constants = [
            [constant: "PRODUCT_WIDGET", key: REFERBOARD, value: "allow_referboard_feature"]
    ]

    Map initialData = SiteConfig.INITIAL_DATA[REFERBOARD] = [
            is_enabled: "false",
            api_key   : ""
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        LicenseConstants.addConstant(license_constants)
        ConfigService.addTab(REFERBOARD, [
                url        : "referboard/config",
                message_key: "referboard"
        ])
        if (SiteConfig.countByType(REFERBOARD) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: REFERBOARD, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(REFERBOARD)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeAutoGeneratePageWidget(NamedConstants.AUTO_GENERATED_PAGE_WIDGET.PRODUCT_WIDGET, REFERBOARD)
            util.removeSiteConfig(REFERBOARD)
            LicenseConstants.removeConstant(license_constants)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin referboard From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(ProductWidgetService).metaClass.mixin RPWS

        TenantContext.eachParallelWithWait(tenantInit)
    }
}

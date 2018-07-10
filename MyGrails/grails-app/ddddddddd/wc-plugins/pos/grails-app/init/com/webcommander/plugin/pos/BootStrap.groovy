package com.webcommander.plugin.pos

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String POS = "pos"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "POS", value: POS],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: POS, value: true],
    ]

    Map config = [
        default_customer: "",
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(POS, [
            url: "posAdmin/loadConfig",
            message_key: "pos"
        ])
        if(!SiteConfig.findByType(DomainConstants.SITE_CONFIG_TYPES.POS)) {
            config.each {
                new SiteConfig(type: POS, configKey: it.key, value: it.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(POS)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(POS)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin pos From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
       TenantContext.eachParallelWithWait(tenantInit)
    }
}

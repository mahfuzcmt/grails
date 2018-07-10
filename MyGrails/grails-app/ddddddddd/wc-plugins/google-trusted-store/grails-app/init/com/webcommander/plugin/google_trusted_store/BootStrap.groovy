package com.webcommander.plugin.google_trusted_store

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private final String GOOGLE_TRUSTED_STORE = "google_trusted_store"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "GOOGLE_TRUSTED_STORE", value: GOOGLE_TRUSTED_STORE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: GOOGLE_TRUSTED_STORE, value: true],
    ]

    Map configs = [
            "store_id": "",
            "badge_position": "BOTTOM_RIGHT",
            "badge_container": "",
            "locale": "en_AU",
            "ship_date_after": "3",
            "deliver_date_after": "4"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(GOOGLE_TRUSTED_STORE, [
                url: "googleTrustedStore/loadConfig",
                message_key: "google.trusted.store",
                license: "allow_google_trusted_store_feature",
                ecommerce  : true
        ]);
        if(!SiteConfig.findByType(GOOGLE_TRUSTED_STORE)) {
            configs.each {key, value ->
                new SiteConfig(type: GOOGLE_TRUSTED_STORE, configKey: key, value: value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(GOOGLE_TRUSTED_STORE)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(GOOGLE_TRUSTED_STORE)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin google-trusted-store From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

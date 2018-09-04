package com.webcommander.plugin.google_analytics_reporting

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    private final String GOOGLE_ANALYTICS = "googleAnalytics"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "GOOGLE_ANALYTICS", value: "google_analytics"],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "google_analytics_reporting", value: true],
    ]

    def analytics_setting_data = [
            'application_name': '',
            'client_id': '',
            'client_secret': '',
            'refresh_token': '',
            'access_token': '',
            profile: null
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(GOOGLE_ANALYTICS, [
            url: "googleAnalytics/loadGoogleAnalyticsSetting",
            message_key: "google.analytics"
        ])
        if(SiteConfig.findAllByType("google_analytics").size() == 0) {
            analytics_setting_data.each { entry ->
                new SiteConfig(type: "google_analytics", configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(GOOGLE_ANALYTICS)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("google_analytics")
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin google-analytics-reporting From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

package com.webcommander.plugin.youtube

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.plugin.youtube.mixin_service.WidgetService as YWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders


class BootStrap {


    private static final YOUTUBE = "youtube"

    List domain_constants = [
            [constant: "SITE_CONFIG_TYPES", key: "YOUTUBE", value: YOUTUBE],
            [constant: "WIDGET_TYPE", key: "YOUTUBE", value: YOUTUBE],
    ]

    List named_constants = [
            [constant: "WIDGET_MESSAGE_KEYS", key: YOUTUBE + ".title", value: "youtube.widget"],
            [constant: "WIDGET_MESSAGE_KEYS", key: YOUTUBE + ".label", value: "youtube"],
            [constant: "WIDGET_LICENSE", key: YOUTUBE, value: "allow_youtube_feature"],
    ]


    Map initialData = SiteConfig.INITIAL_DATA.YOUTUBE = [
            api_key: "AIzaSyBpbOIttWv-z8ewaMk7I519xs_UA5rsyZ4"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)

        ConfigService.addTab(YOUTUBE, [
            url: "youtube/config",
            message_key: "youtube"
        ])

        if (SiteConfig.countByType(YOUTUBE) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: YOUTUBE, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(YOUTUBE)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(YOUTUBE)
            util.removeWidget(YOUTUBE)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin youtube From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin YWS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

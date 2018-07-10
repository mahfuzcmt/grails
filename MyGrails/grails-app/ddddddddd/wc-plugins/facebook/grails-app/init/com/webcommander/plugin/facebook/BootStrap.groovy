package com.webcommander.plugin.facebook

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.plugin.facebook.controllers.admin.design.WidgetController
import com.webcommander.plugin.facebook.mixin_service.WidgetService as FWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.ControllerMixin
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

/**
 * Created by sanjoy on 6/17/2014.
 */
class BootStrap {

    private final String FACEBOOK = "facebook"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "FACEBOOK", value: FACEBOOK],
            [constant:"SITE_CONFIG_TYPES", key: "FACEBOOK", value: FACEBOOK]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: FACEBOOK + ".title", value:"facebook.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: FACEBOOK + ".label", value:FACEBOOK],
            [constant:"WIDGET_LICENSE", key: FACEBOOK, value:"allow_facebook_feature"]
    ]

    Map initialData = [
            appId    : null,
            appSecret: null
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        ConfigService.addTab(FACEBOOK, [
                url: "facebook/config",
                message_key: "facebook"
        ])
        if(SiteConfig.countByType(FACEBOOK) == 0){
            initialData.each {entry ->
                new SiteConfig(type: FACEBOOK, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(FACEBOOK)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(FACEBOOK)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin Facebook From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        ControllerMixin.mixinActions(com.webcommander.controllers.admin.design.WidgetController, WidgetController)
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin FWS
        TenantContext.eachParallelWithWait(tenantInit)
    }

}

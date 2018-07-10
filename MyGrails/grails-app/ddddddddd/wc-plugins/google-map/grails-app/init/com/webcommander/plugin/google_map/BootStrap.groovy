package com.webcommander.plugin.google_map

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.tenant.TenantContext
import com.webcommander.plugin.google_map.mixin_service.WidgetService as GMWS
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders
import org.apache.commons.io.FileUtils

class BootStrap {

    private final String GOOGLE_MAP = "googleMap"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "GOOGLE_MAP", value: GOOGLE_MAP],
            [constant:"SITE_CONFIG_TYPES", key: "GOOGLE_MAP", value: "google_map"]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: GOOGLE_MAP + ".title", value:"google.map.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: GOOGLE_MAP + ".label", value:"google.map"],
            [constant:"WIDGET_LICENSE", key: GOOGLE_MAP, value:"allow_google_map_feature"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        if(!SiteConfig.findByType(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_MAP)) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.GOOGLE_MAP, configKey: "api_key", value: "AIzaSyAnuT05x7qP92GOezatuNLCEf1F1dlUI60").save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_MAP)
            util.removeWidget("googleMap")
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
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin GMWS
        TenantContext.eachParallelWithWait(tenantInit)

        AppEventManager.on("googleMap-widget-after-drop", { widget ->
            File resource = new File(Holders.servletContext.getRealPath("resources") + "/google-map-widget/" + widget.uuid);
            if(resource.exists()) {
                FileUtils.deleteDirectory(resource);
            }
        })
    }
}


package com.webcommander.plugin.location

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.tenant.TenantContext
import com.webcommander.plugin.location.mixin_service.WidgetService as LocationWidgetService
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders
import org.apache.commons.io.FileUtils

class BootStrap {

    private final String LOCATION = "location"
    private final String LOCATION_WIDGET_DEFAULT_ADDRESS = "location_widget_default_address"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "LOCATION", value: LOCATION],
            [constant:"SITE_CONFIG_TYPES", key: "LOCATION", value: LOCATION],
            [constant:"SITE_CONFIG_TYPES", key: "LOCATION_WIDGET_DEFAULT_ADDRESS", value: LOCATION_WIDGET_DEFAULT_ADDRESS]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: LOCATION + ".title", value:"location.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: LOCATION + ".label", value:"location"],
//            [constant:"WIDGET_LICENSE", key: LOCATION, value:"allow_location_feature"],
    ]

    List site_config_constants = [
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "name", value: "South Yarra VIC 3141"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "city", value: "South Yarra"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "postCode", value: "3141"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "formattedAddress", value: "South Yarra VIC 3141, Australia"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "locationAddress", value: "South Yarra VIC 3141, Australia"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "latitude", value: "-37.837544"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "longitude", value: "145"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "contactEmail", value: "support@webalive.com.au"],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "description", value: "Powerful and flexible, WebCommander is designed to take you all the way."],
            [constant:LOCATION_WIDGET_DEFAULT_ADDRESS, key: "api_key", value: "AIzaSyAnuT05x7qP92GOezatuNLCEf1F1dlUI60"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)

        ConfigService.addTab(LOCATION, [
                url: "locationAdmin/config",
                message_key: "location"
        ])

        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeWidget(LOCATION)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
            site_config_constants.each { it ->
                util.removeSiteConfig(it.constant, it.key)
            }
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin location From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin LocationWidgetService
        TenantContext.eachParallelWithWait(tenantInit)
        AppEventManager.on("location-widget-after-drop", { widget ->
            File resource = new File(Holders.servletContext.getRealPath("resources") + "/location-widget/" + widget.uuid);
            if(resource.exists()) {
                FileUtils.deleteDirectory(resource);
            }
        })
    }
}

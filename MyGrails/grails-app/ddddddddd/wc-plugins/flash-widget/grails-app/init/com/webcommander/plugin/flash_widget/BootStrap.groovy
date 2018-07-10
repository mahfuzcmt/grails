package com.webcommander.plugin.flash_widget

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.plugin.flash_widget.mixin_service.TemplateDataProviderService as TDPS
import com.webcommander.plugin.flash_widget.mixin_service.TemplateInstallationService as TIS
import com.webcommander.plugin.flash_widget.mixin_service.WidgetService as FWS
import com.webcommander.plugin.flash_widget.util.WidgetDropper as WD
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.util.widget.WidgetDropper
import grails.util.Holders

class BootStrap {

    static final FLASH = "flash"

    List domain_constants = [
            [constant: "WIDGET_TYPE", key: FLASH, value: FLASH]
    ]

    List named_constants = [
        [constant: "WIDGET_MESSAGE_KEYS", key: FLASH + ".title", value: "flash.widget"],
        [constant: "WIDGET_MESSAGE_KEYS", key: FLASH + ".label", value: "flash"],
        [constant: "WIDGET_LICENSE", key: FLASH, value: "allow_flash_widget_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeWidget(FLASH)
            FlashWidgetResourceTagLib.RESOURCES_PATH.each { resource ->
                util.deleteResourceFolders(resource.value)
            }
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin Flash Widget From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin FWS
        Holders.grailsApplication.mainContext.getBean(TemplateDataProviderService).metaClass.mixin TDPS
        Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).metaClass.mixin TIS
        WidgetDropper.mixin(WD)
        TenantContext.eachParallelWithWait(tenantInit)
    }

    def destroy = {
    }
}

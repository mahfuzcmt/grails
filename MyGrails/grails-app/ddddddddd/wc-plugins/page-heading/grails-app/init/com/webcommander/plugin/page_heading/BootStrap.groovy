package com.webcommander.plugin.page_heading

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.plugin.page_heading.mixin_service.WidgetService as YWS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

class BootStrap {

    private static final PLUGIN_NAME = "pageHeading"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "PAGE_HEADING", value: PLUGIN_NAME]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: PLUGIN_NAME + ".title", value: "page.heading.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: PLUGIN_NAME + ".label", value: "page.heading"],
            [constant:"WIDGET_LICENSE", key: PLUGIN_NAME, value: "allow_page_heading_feature"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeWidget(PLUGIN_NAME)
            DomainConstants.removeConstant(domain_constants)
            NamedConstants.removeConstant(named_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin page heading From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin YWS
        TenantContext.eachParallelWithWait(tenantInit)
    }

    def destroy = {
    }
}

package com.webcommander.plugin.comment_in_checkout

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

import java.util.logging.Level

class BootStrap {
    private static final COMMENT_IN_CHECKOUT = "comment_in_checkout"
    List domain_constants = [
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: COMMENT_IN_CHECKOUT, value: true],
    ]
    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        if(!SiteConfig.createCriteria().count { eq "type", DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE; eq "configKey", "comment_in_checkout" }) {
            new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, configKey: "comment_in_checkout", value: "").save()
        }
    }

    def tenantDestroy = { tenant ->
        DomainConstants.removeConstant(domain_constants)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig("checkout_page", "comment_in_checkout")
        } catch(Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin comment-in-checkout From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}
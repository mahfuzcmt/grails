package com.webcommander.plugin.my_shopping

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String MY_SHOPPING = "my_shopping"
    private final String my_shopping = "myShopping"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "MY_SHOPPING", value: MY_SHOPPING],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "my_shopping", value: true],
    ]

    Map config = [
            tax_code: "none",
            submit_variations: "false",
            submit_combination: "false"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(my_shopping, [
                url: "myShoppingAdmin/loadConfig",
                message_key: "my.shopping",
                ecommerce  : true
        ])
        if(!SiteConfig.findAllByType(MY_SHOPPING)) {
            config.each { entry ->
                new SiteConfig(type: MY_SHOPPING, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(my_shopping)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(MY_SHOPPING)
            DomainConstants.removeConstant(domain_constants)
            util.removeFoldersFromModifiableResource(MyShoppingService.MY_SHOPPING_CATEGORY)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin my-shopping From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

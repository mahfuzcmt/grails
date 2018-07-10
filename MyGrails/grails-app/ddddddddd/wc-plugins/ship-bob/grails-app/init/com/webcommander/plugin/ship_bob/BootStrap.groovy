package com.webcommander.plugin.ship_bob

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String SHIP_BOB = "ship_bob"
    private final String ship_bob = "shipBob"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "SHIP_BOB", value: SHIP_BOB],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "ship_bob", value: true],
    ]

    Map initialData = SiteConfig.INITIAL_DATA[SHIP_BOB] = [
            is_enabled: "false",
            api_key: "",
            shipping_option: '1'
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(ship_bob, [
                url: "shipBob/config",
                message_key: "ship.bob"
        ])
        if (SiteConfig.countByType(SHIP_BOB) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: SHIP_BOB, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(ship_bob)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(SHIP_BOB)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin ship-bob From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

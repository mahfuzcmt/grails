package com.webcommander.plugin.get_price

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String GET_PRICE = "get_price"
    private final String get_price = "getPrice"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "GET_PRICE", value: GET_PRICE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: GET_PRICE, value: true],
    ]

    Map configs = [
            tax_code: '0',
            submit_variations: "true",
            show_out_of_stock_product: "false"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(get_price, [
                url: "getPriceAdmin/loadConfig",
                message_key: "get.price"
        ])

        if(!SiteConfig.findAllByType(DomainConstants.SITE_CONFIG_TYPES.GET_PRICE)) {
            configs.each {
                new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.GET_PRICE, configKey: it.key, value: it.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(get_price)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(GET_PRICE)
            DomainConstants.removeConstant(domain_constants)
            util.removeFoldersFromModifiableResource(GetPriceService.GET_PRICE_CATEGORY)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin get-price From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

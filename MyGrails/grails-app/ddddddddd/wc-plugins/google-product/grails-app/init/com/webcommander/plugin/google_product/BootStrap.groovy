package com.webcommander.plugin.google_product

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CacheManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String GOOGLE_PRODUCT = "google_product"
    private final String google_product = "googleProduct"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "GOOGLE_PRODUCT", value: GOOGLE_PRODUCT],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: GOOGLE_PRODUCT, value: true],
    ]

    Map config = [
            id_mapping: "sku",
            description_mapping: "text",
            submit_additional_image: "false",
            submit_sale_price: "false",
            if_identifier_rules_fail: "send_false",
            submit_feed_expiry_date: "false",
            feed_expiry_date: "30",
            submit_variations: "false",
            exclude_out_of_stock_product: "false",
            add_tax: "false",
            tax_code: "",
            submit_shipping_height: "false",
            submit_shipping_width: "false",
            submit_shipping_length: "false",
            submit_shipping_weight: "false",
            submit_gtin: "false",
            submit_mpn: "false"
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(google_product, [
                url: "googleProductAdmin/loadConfig",
                message_key: "google.product",
                ecommerce  : true
        ])
        if(!SiteConfig.findAllByType(GOOGLE_PRODUCT)) {
            config.each { entry ->
                new SiteConfig(type: GOOGLE_PRODUCT, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(google_product)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(GOOGLE_PRODUCT)
            util.removeFoldersFromModifiableResource(GoogleProductService.GOOGLE_CATEGORY)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin google-product From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }
    
    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

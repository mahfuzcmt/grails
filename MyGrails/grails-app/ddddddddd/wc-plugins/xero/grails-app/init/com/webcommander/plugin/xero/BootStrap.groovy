package com.webcommander.plugin.xero

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Zone
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String XERO = "xero"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "XERO", value: XERO],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "xero", value: true],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(XERO, [
                url: "xero/config",
                message_key: "xero",
                ecommerce: true
        ])
        Map configMap = [
                //client setting
                consumer_key                           : "",
                consumer_secret                        : "",
                private_key                            : "",
                organisation_id                        : "",
                //item setting
                "update_item_name"                     : "true",
                "update_item_description"              : "true",
                "update_item_base_price"               : "true",
                "update_item_cost_price"               : "true",
                "update_item_tax"                      : "true",
                "default_item_purchase_details_account": "",
                "default_item_sales_details_account"   : "",
                //tax setting
                "update_tax"                           : "false",
                "tax_default_zone"                     : Zone.first().id,
                //customer settings
                "update_customer"                      : "true",
                "email"                                : "PrimaryPerson",
                "phone"                                : "Phone",
                "mobile"                               : "Mobile",
                "fax"                                  : "Fax",
                "address"                              : "Postal",
                "activeShippingAddress"                : "Postal",
                "activeBillingAddress"                 : "Postal",
                //Order Setting
                "guest_customer"                       : "",
                "default_customer"                     : "",
                "default_product"                      : "",
                "default_account"                      : "",
                "default_payment_account"              : "",
                "payment_account_mapping"              : "",
                enable_surcharge_sync                  : "false",
                surcharge_account                      : "",
                shipping_account                       : "",
                order_sync_type                       : "detail",
        ];
        if (!SiteConfig.findByType(XERO)) {
            configMap.each {
                new SiteConfig(configKey: it.key, type: XERO, value: it.value).save();
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(XERO)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(XERO)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin xero From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

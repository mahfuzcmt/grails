package com.webcommander.plugin.myob

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Zone
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil

/**
 * Created by sanjoy on 3/3/14.
 */
class BootStrap {

    private final String MYOB = "myob"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "MYOB", value: MYOB],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "myob", value: true],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(MYOB, [
                url        : "myob/config",
                message_key: "myob.config",
                ecommerce  : true
        ])
        Map initialData = SiteConfig.INITIAL_DATA[MYOB] = [
                app_version                   : "19.x",

                //for version 19
                odbc_driver_name              : "",
                company_file_path             : "",
                applictaion_path              : "",
                license_key                   : "17EFD07D4C706B09EB2A8A85C7E3D31D92886DF899B6603F67F4DD7CA74A2DE2379DFB2B472B8BFB0400C63BFF36B50F5D4F91678E8FE4C5C0798D12A747A5BC",
                system_date_format            : "",

                //for version 21
                client_id                     : null,
                client_secret                 : null,
                redirect_uri                  : null,
                refresh_token                 : null,
                company_file_uri              : null,
                auth_code                     : null,
                company_file_username         : "Administrator",
                company_file_password         : "",
                //customer settings
                update_customer               : "false",
                customer_phone                : "Phone1",
                customer_mobile               : "Phone2",
                customer_tax                  : "",
                //product settings
                update_product_name           : "false",
                update_product_description    : "false",
                update_product_tax            : "false",
                update_product_baseprice      : "false",
                update_product_costprice      : "false",
                update_product_category       : "false",
                update_product_manufacturer   : "false",
                update_product_stock          : "false",
                update_product_inventory      : "false",
                update_product_image          : "false",
                product_income_account        : "",
                product_expense_account       : "",
                product_costofsale_account    : "",
                product_asset_account         : "",
                product_purchase_account      : "",
                product_default_tax           : "",
                mapping_custom_list_1         : "",
                mapping_custom_list_2         : "",
                mapping_custom_list_3         : "",
                mapping_custom_field_1        : "",
                mapping_custom_field_2        : "",
                mapping_custom_field_3        : "",

                //tax settings
                update_tax                    : "false",
                tax_default_zone              : Zone.first().id,

                // Order Setting
                guest_customer                : "",
                default_customer              : "",
                default_product               : "",
                enable_surcharge_sync         : "",
                default_surcharge_line_product: "",
                // payment account
                "default_payment_account"     : "",
                "payment_account_mapping"     : "",
                "order_sync_type"             : "detail",
                "last_sync_summary_id"        : ""
        ]

        if (SiteConfig.countByType(MYOB) == 0) {
            initialData.each { entry ->
                new SiteConfig(type: MYOB, configKey: entry.key, value: entry.value).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(MYOB)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(MYOB)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin myob From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

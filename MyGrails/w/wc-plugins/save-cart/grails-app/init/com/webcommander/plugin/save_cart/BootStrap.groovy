package com.webcommander.plugin.save_cart

import com.webcommander.admin.ConfigService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private final String SAVE_CART = "save_cart"
    private final String SAVE_CART_ACTIVE = "save_cart_active"
    private final String SAVE_CART_LABEL = "save_cart_label"
    private final String SAVE_CART_DEFAULT_LABEL = "save_cart_default_label"
    private final String SAVE_CART_ORDER = "save_cart_order"
    ConfigService configService

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "SAVE_CART", value: SAVE_CART],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: SAVE_CART, value: true],
    ]

    Map config = [
        enabled: "true"
    ]

    List site_config_constants = [
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_label", value: "My Carts"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_default_label", value: "My Carts"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_active", value: "true"],
            [constant:DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: SAVE_CART_LABEL, value:"Saved Cart"],
            [constant:DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: SAVE_CART_DEFAULT_LABEL, value:"Saved Cart"],
            [constant:DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: SAVE_CART_ACTIVE, value:"true"],
            [constant:DomainConstants.SITE_CONFIG_TYPES.MY_ACCOUNT_PAGE, key: SAVE_CART, value: "true"],
            [constant:SAVE_CART, key: "enabled", value: "true"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(SAVE_CART, [
                url: "saveCartAdmin/loadConfig",
                message_key: "save.cart",
                ecommerce  : true
        ])

        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
        if(!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, SAVE_CART_ORDER)){
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, SAVE_CART_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.MY_CARTS
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        }
        if(!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")){
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE,"my_carts_order")
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(SAVE_CART)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(SAVE_CART)
            site_config_constants.each { it ->
                if(it.constant == DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE && configService.getSortedFields(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS)).size() == 1){
                    util.removeSiteConfig(it.constant, it.key)
                }  else if(it.constant != DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE) {
                    util.removeSiteConfig(it.constant, it.key)
                }
            }
            DomainConstants.removeConstant(domain_constants)
            if(configService.getSortedFields(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS)).size() == 1){
                util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")
            }
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, SAVE_CART_ORDER)
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, SAVE_CART_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin save_cart From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }

    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

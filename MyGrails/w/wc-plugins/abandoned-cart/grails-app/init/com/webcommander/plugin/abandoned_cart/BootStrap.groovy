package com.webcommander.plugin.abandoned_cart

import com.webcommander.admin.ConfigService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import groovy.util.logging.Log
import java.util.logging.Level


@Log
class BootStrap {

    private static final ABANDONED_CART = "abandoned_cart"
    private static final ABANDONED_CART_IDENTIFIRE = "abandoned-cart"
    private static final ABANDONED_CART_ACTIVE = "abandoned_cart_active"
    private static final ABANDONED_CART_LABEL = "abandoned_cart_label"
    private static final ABANDONED_CART_DEFAULT_LABEL = "abandoned_cart_default_label"
    private static final ABANDONED_CART_ORDER = "abandoned_cart_order"
    ConfigService configService

    List domain_constants = [
            [constant: "SITE_CONFIG_TYPES", key: "ABANDONED_CART", value: ABANDONED_CART],
            [constant: "ECOMMERCE_EMAIL_TEMPLATE_CHECKLIST", key: ABANDONED_CART, value: true],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: ABANDONED_CART, value: true],
    ]

    List site_config_constants = [
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_label", value: "My Carts"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_default_label", value: "My Carts"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, key: "my_carts_active", value: "true"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: ABANDONED_CART_LABEL, value: "Abandoned Cart"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: ABANDONED_CART_DEFAULT_LABEL, value: "Abandoned Cart"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, key: ABANDONED_CART_ACTIVE, value: "true"],
            [constant: DomainConstants.SITE_CONFIG_TYPES.MY_ACCOUNT_PAGE, key: ABANDONED_CART, value: "false"],
            [constant: ABANDONED_CART, key: "interval_type", value: "day"],
            [constant: ABANDONED_CART, key: "interval", value: 30],
            [constant: ABANDONED_CART, key: "no_of_max_time", value: 0],
    ]


    def tenantInit = { tenant ->

        Map emailTemplateData = [
                label              : "abandoned.cart",
                identifier         : ABANDONED_CART_IDENTIFIRE,
                subject            : "Incomplete Order Notification # %cart_id%",
                isCcToAdminReadonly: false,
                type               : DomainConstants.EMAIL_TYPE.CUSTOMER
        ]

        ConfigService.addTab(ABANDONED_CART, [
                url        : "abandonedCartAdmin/config",
                message_key: "abandoned.cart",
                ecommerce  : true
        ])

        DomainConstants.addConstant(domain_constants)
        if (!EmailTemplate.findByIdentifier(ABANDONED_CART_IDENTIFIRE)) {
            new EmailTemplate(emailTemplateData).save()
        }

        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
        if(!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, ABANDONED_CART_ORDER)){
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, ABANDONED_CART_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.MY_CARTS
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE

        }
        if(!SiteConfig.findAllByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")){
            configService.setOrder(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE,"my_carts_order")
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(ABANDONED_CART)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            site_config_constants.each { it ->
                if (it.constant == DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE && configService.getSortedFields(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS)).size() == 1) {
                    util.removeSiteConfig(it.constant, it.key)
                }  else if(it.constant != DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE) {
                    util.removeSiteConfig(it.constant, it.key)
                }
            }
            util.removeEmailTemplates(ABANDONED_CART_IDENTIFIRE)
            DomainConstants.removeConstant(domain_constants)
            if(configService.getSortedFields(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS)).size() == 1){
                util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")
            }
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "my_carts_order")
            util.removeSiteConfig(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS,  ABANDONED_CART_ORDER)
            configService.reorderFields(DomainConstants.SITE_CONFIG_TYPES.MY_CARTS, ABANDONED_CART_ORDER)
            AppUtil.clearConfig DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin abandoned-cart From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }


    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
        AppUtil.getBean(AbandonedCartService).startScheduler()
    }
}

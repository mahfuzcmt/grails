package com.webcommander.plugin.subscription_from_checkout

import com.webcommander.admin.Customer
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.AddressData
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webmarketing.NewsletterService
import com.webcommander.webmarketting.NewsletterSubscriber
import grails.util.Holders

class BootStrap {

    List domain_constants = [
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "subscription_from_checkout", value: true],
    ]

    List site_config_constants = [
            [constant:DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, key: "enable_newsletter_subscription", value: "true"],
            [constant:DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, key: "subscribe_news_letter", value: "I want to subscribe to newsletter"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        site_config_constants.each { it ->
            if (!SiteConfig.findAllByTypeAndConfigKey(it.constant, it.key)) {
                new SiteConfig(type: it.constant, configKey: it.key, value: it.value).save()
                AppUtil.clearConfig it.constant
            }
        }
    }

    def tenantDestroy = { tenant ->
        DomainConstants.removeConstant(domain_constants)
        PluginDestroyUtil util = new PluginDestroyUtil()
        site_config_constants.each { it ->
            util.removeSiteConfig(it.constant, it.key)
        }
    }

    def init = { servletContext ->
        AppEventManager.on("before-order-confirm", { params ->
            def session = AppUtil.session
            if(params?.subscribe) {
                Map properties = [:]
                if(session.customer) {
                    Customer customer = Customer.get(session.customer)
                    properties.firstName = customer.firstName
                    properties.lastName = customer.lastName
                    properties.email = Customer.get(session.customer).address.email
                } else {
                    AddressData billingAddress = session.effective_billing_address
                    properties.firstName = billingAddress.firstName
                    properties.lastName = billingAddress.lastName
                    properties.email = billingAddress.email
                }
                def id = NewsletterSubscriber.where {
                    email == properties.email
                    isSubscribed == true
                }.count()

                if (!id) {
                    NewsletterService newsletterService = Holders.applicationContext.getBean("newsletterService")
                    newsletterService.subscribeNewsletter(properties)
                }
            }
        })
        SiteConfig.INITIAL_DATA[DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE].put("enable_newsletter_subscription", "true")
        SiteConfig.INITIAL_DATA[DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE].put("subscribe_news_letter","I want to subscribe to newsletter")
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

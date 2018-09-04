package com.webcommander.plugin.awaiting_payment_notification

import com.webcommander.admin.ConfigService
import com.webcommander.config.EmailTemplate
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

import java.util.logging.Level

class BootStrap {

    private final String AWAITING_PAYMENT= "awaiting_payment"

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "AWAITING_PAYMENT", value: AWAITING_PAYMENT],
            [constant:"ECOMMERCE_PLUGIN_CHECKLIST", key: "awaiting_payment_notification", value: true],
    ]

    Map configs = [
            no_of_max_time: 10,
            interval      : 5,
            interval_type : "day"
    ]
    Map emailTemplateData = [
            label              : "awaiting.payment",
            identifier         : "awaiting-payment",
            subject            : "Your payment is awaiting for the order %order_id%",
            isCcToAdminReadonly: false,
            type               : DomainConstants.EMAIL_TYPE.PAYMENT
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(AWAITING_PAYMENT, [
                url        : "awaitingPaymentNotification/config",
                message_key: "awaiting.payment",
                ecommerce  : true
        ])

        if (!SiteConfig.findByType(DomainConstants.SITE_CONFIG_TYPES.AWAITING_PAYMENT)) {
            configs.each {
                new SiteConfig(type: DomainConstants.SITE_CONFIG_TYPES.AWAITING_PAYMENT, configKey: it.key, value: it.value).save()
            }
        }

        if (!EmailTemplate.findByIdentifier("awaiting-payment")) {
            new EmailTemplate(emailTemplateData).save()
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(AWAITING_PAYMENT)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeSiteConfig(AWAITING_PAYMENT)
            util.removeEmailTemplates("awaiting-payment")
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin awaiting-payment-notification From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)

        Holders.grailsApplication.mainContext.getBean(AwaitingPaymentEmailService).startScheduler()
    }
}

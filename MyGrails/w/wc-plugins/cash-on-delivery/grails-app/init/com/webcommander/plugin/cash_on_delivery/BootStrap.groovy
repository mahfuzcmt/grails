package com.webcommander.plugin.cash_on_delivery

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.payment.PaymentService
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGateway
import grails.util.Holders
import com.webcommander.tenant.TenantContext
import com.webcommander.plugin.cash_on_delivery.mixin_service.PaymentService as CODPS
import groovy.util.logging.Log

import java.util.logging.Level

/**
 * Created by sanjoy on 12/08/2014.
 */
@Log
class BootStrap {

    private final String CASH_ON_DELIVERY = "COD"

    List domain_constants = [
            [constant:"PAYMENT_GATEWAY_CODE", key: "CASH_ON_DELIVERY", value: CASH_ON_DELIVERY],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "cash_on_delivery", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: CASH_ON_DELIVERY, value: "allow_cash_on_delivery_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if (PaymentGateway.countByCode(CASH_ON_DELIVERY) == 0) {
            new PaymentGateway(
                    code: CASH_ON_DELIVERY,
                    name: "cash.on.delivery"
            ).save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removePaymentGateway(CASH_ON_DELIVERY)
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin cash-on-delivery From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin CODPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}
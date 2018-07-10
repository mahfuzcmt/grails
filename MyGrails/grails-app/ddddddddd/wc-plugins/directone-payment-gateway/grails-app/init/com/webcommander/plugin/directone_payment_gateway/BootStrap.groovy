package com.webcommander.plugin.directone_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.directone_payment_gateway.mixin_service.payment.PaymentService as DOPS
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import com.webcommander.tenant.TenantContext
import groovy.util.logging.Log

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

import java.util.logging.Level

@Log
class BootStrap {

    private final String DIRECTONE = "DIRECTONE"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "DIRECTONE", value: DIRECTONE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "directone_payment_gateway", value: true],
    ]
    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        if (PaymentGatewayMeta.createCriteria().count {
            eq("fieldFor", DIRECTONE)
        } == 0) {
            new PaymentGatewayMeta(fieldFor: DIRECTONE, label: "payment.type", name: "type",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", optionLabel: ["f:NET", "f:API"]
                    , optionValue: ["NET", "API"]
            ).save()
            PaymentGatewayMeta mode = new PaymentGatewayMeta(fieldFor: DIRECTONE, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test", optionLabel: ["test", "live"], optionValue: ["test", "live"]
            ).save()
            new PaymentGatewayMeta(fieldFor: DIRECTONE, label: "f:Vendor Name", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: DIRECTONE,
                    label: "f:Vendor Password", name: "password",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:DirectOne")
                creditCard.optionValue.add(DIRECTONE)
            } else {
                creditCard.optionLabel = ["f:DiectOne"]
                creditCard.optionValue = [DIRECTONE]
                creditCard.value = DIRECTONE
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(DIRECTONE, "f:DirectOne")
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin abandoned-cart From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin DOPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

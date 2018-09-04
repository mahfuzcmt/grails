package com.webcommander.plugin.braintree_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.braintree_payment_gateway.mixin_service.payment.PaymentService as BRPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import groovy.util.logging.Log

import java.util.logging.Level

@Log
class BootStrap {

    private final String BRAINTREE = "BRAINTREE"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "BRAINTREE", value: BRAINTREE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "braintree_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + BRAINTREE, value: "allow_braintree_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if (PaymentGatewayMeta.createCriteria().count {
            eq("fieldFor", BRAINTREE)
        } == 0) {
            new PaymentGatewayMeta(fieldFor: BRAINTREE, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test",
                    optionLabel: ["sandbox", "live"], optionValue: ["sandbox", "live"]
            ).save()
            new PaymentGatewayMeta(fieldFor: BRAINTREE, label: "f:Merchant ID", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: BRAINTREE,
                    label: "f:Public Key", name: "public_key",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            new PaymentGatewayMeta(fieldFor: BRAINTREE,
                    label: "f:Private Key", name: "private_key",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:BrainTree")
                creditCard.optionValue.add(BRAINTREE)
            } else {
                creditCard.optionLabel = ["f:BrainTree"]
                creditCard.optionValue = [BRAINTREE]
                creditCard.value = BRAINTREE
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("BRAINTREE", "f:BrainTree")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin abandoned-cart From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin BRPS

        TenantContext.eachParallelWithWait(tenantInit)
    }
}

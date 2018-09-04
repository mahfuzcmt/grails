package com.webcommander.plugin.stripe_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.stripe_payment_gateway.mixin_service.payment.PaymentService as SPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {


    private static final String STRIPE = "STRIPE"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "STRIPE", value: STRIPE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "stripe_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + STRIPE, value: "allow_stripe_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(PaymentGatewayMeta.createCriteria().count {eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE)} == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE,
                    label: "f:Secret Key", name: "secret_key",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE,
                    label: "f:Public Key", name: "public_key",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if(creditCard.optionLabel) {
                creditCard.optionLabel.add("f:Stripe")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE)
            } else {
                creditCard.optionLabel = ["f:Stripe"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(STRIPE, "f:Stripe")
            DomainConstants.removeConstant(domain_constants)
            LicenseConstants.removeConstant(license_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin SPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

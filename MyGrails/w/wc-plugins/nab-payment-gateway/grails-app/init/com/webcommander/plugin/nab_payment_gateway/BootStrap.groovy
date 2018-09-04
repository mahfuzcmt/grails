package com.webcommander.plugin.nab_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.nab_payment_gateway.mixin_service.payment.PaymentService as NABPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {

    private static final NAB = "NAB"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "NAB", value: NAB],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "nab_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + NAB, value: "allow_nab_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(PaymentGatewayMeta.createCriteria().count { eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB) } == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB, label: "payment.type", name: "type",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", extraAttrs: "toggle-target='NAB'",
                    optionLabel: ["f:NET", "f:API"], optionValue: ["NET", "API"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test", optionLabel: ["test", "live"], optionValue: ["test", "live"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB, label: "f:Merchant ID", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB,
                    label: "f:Transaction Password", name: "transaction_password",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required", clazz: "NAB-API").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if(creditCard.optionLabel) {
                creditCard.optionLabel.add("f:NAB")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB)
            } else {
                creditCard.optionLabel = ["f:NAB"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.NAB
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(NAB, "f:NAB")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin NABPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

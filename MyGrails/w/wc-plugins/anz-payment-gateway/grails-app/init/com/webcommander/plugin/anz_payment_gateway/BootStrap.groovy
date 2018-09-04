package com.webcommander.plugin.anz_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.anz_payment_gateway.mixin_service.payment.PaymentService as ANZPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import groovy.util.logging.Log

import java.util.logging.Level

@Log
class BootStrap {

   private final String ANZ = "ANZ"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "ANZ", value: ANZ],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "anz_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD  + "_" + ANZ, value: "allow_anz_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if (PaymentGatewayMeta.createCriteria().count {
            eq("fieldFor", ANZ)
        } == 0) {
            new PaymentGatewayMeta(fieldFor: ANZ, label: "payment.type", name: "type",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", extraAttrs: "toggle-target='ANZ'",
                    optionLabel: ["f:NET", "f:API"],
                    optionValue: ["NET", "API"]
            ).save()
            new PaymentGatewayMeta(fieldFor: ANZ, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test",
                    optionLabel: ["test", "live"], optionValue: ["test", "live"]
            ).save()
            new PaymentGatewayMeta(fieldFor: ANZ, label: "f:Merchant ID", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: ANZ,
                    label: "f:Access Code", name: "access_code",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:ANZ")
                creditCard.optionValue.add(ANZ)
            } else {
                creditCard.optionLabel = ["f:ANZ"]
                creditCard.optionValue = [ANZ]
                creditCard.value = ANZ
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(ANZ, "f:ANZ")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "plugin anz-payment-gateway for  tenant $tenant not destryed", e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin ANZPS

        TenantContext.eachParallelWithWait(tenantInit)
    }
}

package com.webcommander.plugin.commweb_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.commweb_payment_gateway.mixin_service.payment.PaymentService as CWPS
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

    private final String COMMWEB = "COMMWEB"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "COMMWEB", value: COMMWEB],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "commweb_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + COMMWEB, value: "allow_commweb_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if (PaymentGatewayMeta.createCriteria().count {
            eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB)
        } == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB, label: "payment.type", name: "type", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", extraAttrs: "toggle-target='COMMWEB'",
                    optionLabel: ["f:Merchant Hosted", "f:Server Hosted"]
                    , optionValue: ["merchant", "server"]
            ).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB, label: "f:Merchant ID", name: "merchantId", validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB, label: "f:Access Code", name: "access_code", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required").save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB, label: "f:Secure Hash Secret", name: "secret_code", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, clazz: "COMMWEB-server").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:CommWeb")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB)
            } else {
                creditCard.optionLabel = ["f:CommWeb"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("COMMWEB", "f:CommWeb")
            DomainConstants.removeConstant(domain_constants)
            LicenseConstants.removeConstant(license_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin abandoned-cart From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin CWPS

        TenantContext.eachParallelWithWait(tenantInit)
    }
}

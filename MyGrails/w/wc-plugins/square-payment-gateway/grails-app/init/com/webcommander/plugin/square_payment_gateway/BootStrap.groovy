package com.webcommander.plugin.square_payment_gateway

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.webcommander.admin.ConfigService
import com.webcommander.plugin.square_payment_gateway.mixin_service.payment.PaymentService as SPS
import grails.util.Holders

import java.util.logging.Level

class BootStrap {
    private static final String SQUARE = "SQUARE"

    List domain_constants = [
            [constant: "CARD_PAYMENT_PROCESSOR_CODE", key: "SQUARE", value: SQUARE],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "square_payment_gateway", value: true],
            [constant: "SITE_CONFIG_TYPES", key: "SQUARE", value: SQUARE],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + SQUARE, value: "allow_square_payment_gateway_feature"]
    ]

    Map config = [
            applicationId: "",
            locationId: "",
            authorizationToken: "",
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(!SiteConfig.findByType(SQUARE)) {
            config.each {
                new SiteConfig(type: SQUARE, configKey: it.key, value: it.value).save()
            }
        }
        if (PaymentGatewayMeta.createCriteria().count {
            eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE)
        } == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE, label: "payment.type", name: "type", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "merchant", extraAttrs: "toggle-target='SQUARE'",
                    optionLabel: ["f:Merchant Hosted", "f:Server Hosted"]
                    , optionValue: ["merchant", "server"]
            ).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE, label: "f:Application Id", name: "applicationId", validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE, label: "f:Location Id", name: "locationId", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE, label: "f:Personal Access Token", name: "accessToken", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:Square")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE)
            } else {
                creditCard.optionLabel = ["f:Square"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor("SQUARE", "f:Square")
            DomainConstants.removeConstant(domain_constants)
            LicenseConstants.removeConstant(license_constants)
            util.removeSiteConfig(SQUARE)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin square-payment-gateway From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin SPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

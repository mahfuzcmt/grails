package com.webcommander.plugin.eway_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.eway_payment_gateway.mixin_service.payment.PaymentService as EPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {

    private static final String EWAY = "EWAY"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "EWAY", value: EWAY],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "eway_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + EWAY, value: "allow_eway_payment_gateway_feature"]
    ]


    def tenantInit = { tenant ->

        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)

        if(PaymentGatewayMeta.createCriteria().count { eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY) } == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY, label: "payment.type",
                    name: "type", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "SHARED",
                    optionLabel: ["f:Shared", "f:Direct"], optionValue: ["SHARED", "DIRECT"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test", optionLabel: ["test", "live"], optionValue: ["test", "live"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY, label: "f:Customer Id", name: "customerId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY, label: "f:User Name", name: "userName", validation: "required",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save();

            /*****************************************************Wallet***********************************************/
            new PaymentGatewayMeta(fieldFor: EWAY, label: "f:Enable Wallet Payment", name: "enableWallet", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX,
                    value: "false", extraAttrs: "toggle-target='enable-wallet'").save()
            new PaymentGatewayMeta(fieldFor: EWAY, label: "f:API Key[Wallet]", name: "walletMerchantApiKey", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                    validation: "required", clazz: "enable-wallet").save()
            new PaymentGatewayMeta(fieldFor: EWAY, label: "f:Password[Wallet]", name: "walletMerchantPassword", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD,
                    validation: "required", clazz: "enable-wallet").save()
            /*****************************************************End**************************************************/

            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if(creditCard.optionLabel) {
                creditCard.optionLabel.add("f:eWAY")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY)
            } else {
                creditCard.optionLabel = ["f:eWAY"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.EWAY
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(EWAY, "f:eWAY")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin EPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

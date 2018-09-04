package com.webcommander.plugin.securepay_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.securepay_payment_gateway.mixin_service.payment.PaymentService as SPPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {

    private static final String SECUREPAY = "SECUREPAY"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "SECUREPAY", value: SECUREPAY],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "securepay_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + SECUREPAY, value: "allow_securepay_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(PaymentGatewayMeta.createCriteria().count {eq("fieldFor", SECUREPAY)} == 0) {
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "payment.type", name: "type",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", extraAttrs: "toggle-target='SECUREPAY'",
                    optionLabel: ["f:NET", "f:API"], optionValue: ["NET", "API"]).save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test", optionLabel: ["test", "live"], optionValue: ["test", "live"]).save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "f:Merchant ID", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY,
                    label: "f:Transaction Password", name: "transaction_password",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required").save()

            /*****************************************************Wallet***********************************************/
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "f:Enable Wallet Payment", name: "enableWallet", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX,
                    value: "false", extraAttrs: "toggle-target='enable-wallet'").save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "f:Wallet Mode", name: "walletMode", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT,
                    value: "test", clazz: "enable-wallet", optionLabel: ["test", "live"], optionValue: ["test", "live"]).save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "f:Merchant Id[Wallet]", name: "walletMerchantId", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                    validation: "required", clazz: "enable-wallet").save()
            new PaymentGatewayMeta(fieldFor: SECUREPAY, label: "f:Transection Password[Wallet]", name: "walletMerchantPassword", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD,
                    validation: "required", clazz: "enable-wallet").save()
            /*****************************************************End**************************************************/

            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if(creditCard.optionLabel) {
                creditCard.optionLabel.add("f:SecurePay")
                creditCard.optionValue.add(SECUREPAY)
            } else {
                creditCard.optionLabel = ["f:SecurePay"]
                creditCard.optionValue = [SECUREPAY]
                creditCard.value = SECUREPAY
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(SECUREPAY, 'f:SecurePay')
            DomainConstants.removeConstant(domain_constants)
            LicenseConstants.removeConstant(license_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin SPPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

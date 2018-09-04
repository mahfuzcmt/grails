package com.webcommander.plugin.payway_payment_gateway

import com.webcommander.common.LargeData
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.PathManager
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.payway_payment_gateway.mixin_service.payment.PaymentService as PWPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.util.security.InformationEncrypter
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

class BootStrap {

    private static final PAYWAY = "PAYWAY"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "PAYWAY", value: PAYWAY],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "payway_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + PAYWAY, value: "allow_payway_payment_gateway_feature"]
    ]

    FileService fileService

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(PaymentGatewayMeta.createCriteria().count { eq("fieldFor", DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY) } == 0) {
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "payment.type", name: "type",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "NET", extraAttrs: "toggle-target='payway'",
                    optionLabel: ["f:NET", "f:API"], optionValue: ["NET", "API"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "mode", name: "mode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT, value: "test", optionLabel: ["test", "live"], optionValue: ["test", "live"]).save()
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Merchant ID", name: "merchantId",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:PayPal Email(optional)", name: "payPalEmail",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "email").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Biller Code", name: "billerCode",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required@if{self::visible}", clazz: "payway-NET").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Operator Name", name: "userName",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Password", name: "password",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.PASSWORD, validation: "required").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Encryption Key", name: "encryptionKey",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "required@if{self::visible}", clazz: "payway-NET").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "f:Certificate File", name: "certificateFile",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.FILE, validation: "required@if{self::visible}", clazz: "payway-API").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "payment.notification.email", name: "notificationEmail",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "skip@if{self::hidden} email").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "sts.notification", name: "stsNotification",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX, extraAttrs: "toggle-target='payway-sts'").save();
            new PaymentGatewayMeta(fieldFor: DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY, label: "failed.sts.email", name: "stsFailedEmail",
                    htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT, validation: "skip@if{self::hidden} required email", clazz: "payway-sts").save();

            /*****************************************************Wallet***********************************************/
            new PaymentGatewayMeta(fieldFor: PAYWAY, label: "f:Enable Wallet Payment", name: "enableWallet", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX,
                    value: "false", extraAttrs: "toggle-target='enable-wallet'").save()
            new PaymentGatewayMeta(fieldFor: PAYWAY, label: "f:Merchant Id[Wallet]", name: "walletMerchantId", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                    validation: "required", clazz: "enable-wallet").save()
            new PaymentGatewayMeta(fieldFor: PAYWAY, label: "f:Public API Key[Wallet]", name: "walletMerchantPublicApiKey", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                    validation: "required", clazz: "enable-wallet").save()
            new PaymentGatewayMeta(fieldFor: PAYWAY, label: "f:Private API Key[Wallet]", name: "walletMerchantPrivateApiKey", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT,
                    validation: "required", clazz: "enable-wallet").save()
            /*****************************************************End**************************************************/

            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if (creditCard.optionLabel) {
                creditCard.optionLabel.add("f:PayWay")
                creditCard.optionValue.add(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY)
            } else {
                creditCard.optionLabel = ["f:PayWay"]
                creditCard.optionValue = [DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY]
                creditCard.value = DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(PAYWAY, "f:PayWay")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin PWPS
        TenantContext.eachParallelWithWait(tenantInit)
        AppEventManager.on("PAYWAY-payment-gateway-configuration-update", { params ->
            MultipartFile uploadedFile = params.certificateFile ?: null
            if(!uploadedFile) {
                return
            }
            def filePath = PathManager.getCustomRestrictedResourceRoot("certificates/")
            fileService.uploadFile(uploadedFile, null, "payway.cert", null, filePath)
            File certFile = new File(filePath, "payway.cert")
            InformationEncrypter encrypter = new InformationEncrypter()
            encrypter.hideInfo(certFile.text)
            LargeData paywayCertificateFile = new LargeData(content: encrypter.toString().getBytes("UTF-8"), name: "payway-payment-gateway-payway.cert")
            paywayCertificateFile.save()
        })
    }

    def activate = {

    }

    def deactivate = {

    }
}

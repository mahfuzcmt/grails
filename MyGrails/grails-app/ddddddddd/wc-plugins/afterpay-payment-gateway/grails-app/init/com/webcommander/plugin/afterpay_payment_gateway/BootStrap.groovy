package com.webcommander.plugin.afterpay_payment_gateway

import com.webcommander.AutoGeneratedPage
import com.webcommander.admin.MessageSource
import com.webcommander.common.FileService
import com.webcommander.constants.LicenseConstants
import com.webcommander.design.Layout
import com.webcommander.events.AppEventManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import groovy.util.logging.Log
import org.springframework.web.multipart.MultipartFile
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.afterpay_payment_gateway.mixin_service.payment.PaymentService as APYPS
import grails.util.Holders

import java.util.logging.Level

@Log
class BootStrap {

    static FileService fileService
    static AfterpayService afterpayService
    private static final AFTERPAY = "APY"

    List domain_constants = [
            [constant:"PAYMENT_GATEWAY_CODE", key: "AFTERPAY", value: AFTERPAY],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "afterpay_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: AFTERPAY, value: "allow_afterpay_payment_gateway_feature"]
    ]

    Map siteMessage = [
        "price.installment.amount" : "or 4 installments of %installment_amount% with"
    ]

        def tenantInit = { tenant ->
            DomainConstants.addConstant(domain_constants)
            LicenseConstants.addConstant(license_constants)

            if(!PaymentGateway.findByCode(DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY)) {
                new PaymentGateway(
                        code: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        name: "afterpay",
                        isEnabled: false,
                        isDefault: false,
                        isSurChargeApplicable: false,
                        pendingMessage: "s:your.payment.pending"
                ).save()

            }
            if(PaymentGatewayMeta.createCriteria().count {eq("fieldFor", DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY)} == 0) {
                new PaymentGatewayMeta(
                        fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        label: "Show Installment Amount",
                        name: "showInstallmentAmount",
                        validation: null,
                        htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.CHECK_BOX
                ).save()
                PaymentGatewayMeta apyMeta = new PaymentGatewayMeta(
                        fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        label: "mode",
                        name: "mode",
                        validation: "required",
                        htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.SELECT,
                optionLabel : ["test", "live"],optionValue : ["test", "live"]
                ).save()
                new PaymentGatewayMeta(
                        fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        label: "Merchant ID",
                        name: "merchantId",
                        validation: "required",
                        htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT
                ).save()
                new PaymentGatewayMeta(
                        fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        label: "Merchant Secret Key",
                        name: "merchantSecretKey",
                        validation: "required",
                        htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT
                ).save()
                new PaymentGatewayMeta(
                        fieldFor: DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY,
                        label: "Image",
                        name: "afterpayImage",
                        validation: null,
                        htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.FILE,
                        clazz: "afterpay-image",
                        extraAttrs: "file-type='image'"
                ).save()
            }
            if(!AutoGeneratedPage.findByName("afterpay.payment.gateway")) {
                new AutoGeneratedPage(
                        name: "afterpay.payment.gateway",
                        title: "afterpay.payment.gateway",
                        layout: Layout.first()
                ).save()
            }

        siteMessage.each {
            if( !MessageSource.findByMessageKeyAndLocale(it.key, "all") ) {
                new MessageSource([messageKey: it.key, message: it.value, locale: "all"]).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        try {
            destroyUtil.removePaymentGateway(DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY).removePaymentMeta(DomainConstants.PAYMENT_GATEWAY_CODE.AFTERPAY)
            destroyUtil.removeSiteMessage(*siteMessage.keySet())

            DomainConstants.removeConstant(domain_constants)
            LicenseConstants.removeConstant(license_constants)
        } catch(Exception e) {
            log.log Level.SEVERE, "plugin afterpay-payment-gateway for  tenant $tenant not destryed", e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin APYPS

        TenantContext.eachParallelWithWait(tenantInit)
        AppEventManager.on("APY-payment-gateway-configuration-update", { params ->
            MultipartFile imageFile = params.afterpayImage ?: null
            if (!imageFile) return
            def filePath = new File(PathManager.getResourceRoot("afterpay-payment-gateway/images/"))
            if (filePath.exists()) {
                filePath.deleteDir()
            }
            fileService.uploadFile(imageFile, NamedConstants.RESOURCE_TYPE.RESOURCE, imageFile.originalFilename, null, filePath.toString())
            afterpayService.updateMetaValue("afterpayImage", imageFile.originalFilename)
        })
    }
}
package com.webcommander.plugin.epath_payment_gateway

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.LicenseConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.epath_payment_gateway.payment.PaymentService as EPPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class BootStrap {

    private final String EPATH = "EPATH"

    List domain_constants = [
            [constant:"CARD_PAYMENT_PROCESSOR_CODE", key: "EPATH", value: EPATH],
            [constant: "ECOMMERCE_PLUGIN_CHECKLIST", key: "epath_payment_gateway", value: true],
    ]

    List license_constants = [
            [constant:"PAYMENT_GATEWAY", key: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD + "_" + EPATH , value: "allow_epath_payment_gateway_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        LicenseConstants.addConstant(license_constants)
        if(PaymentGatewayMeta.createCriteria().count { eq("fieldFor", EPATH) } == 0) {
            new PaymentGatewayMeta(fieldFor: EPATH,
                    label: "f:Payment URL", name: "paymentUrl",
                    validation: "required", htmlType: NamedConstants.PAYMENT_GATEWAY_META_FIELD_TYPE.TEXT).save()
            def creditCard = PaymentGatewayMeta.findByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD)
            if(creditCard.optionLabel) {
                creditCard.optionLabel.add("f:EPath")
                creditCard.optionValue.add(EPATH)
            } else {
                creditCard.optionLabel = ["f:EPath"]
                creditCard.optionValue = [EPATH]
                creditCard.value = EPATH
            }
            creditCard.save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeCreditCardProcessor(EPATH, "f:EPath")
            LicenseConstants.removeConstant(license_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(PaymentService).metaClass.mixin EPPS
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

package com.webcommander.plugin.securepay_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.securepay_payment_gateway.SecurePayService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class PaymentService {
    static SecurePayService _securePayService

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }
    static getSecurePayService() {
        if(_securePayService) {
            return _securePayService
        }
        return _securePayService = Holders.grailsApplication.mainContext.getBean(SecurePayService)
    }
    void renderSECUREPAYCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SECUREPAY)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        "render${configMap["type"]}SECUREPAYCRDPaymentPage"(cart, creditCardConfig,  configMap, renderer)
    }

    private void renderNETSECUREPAYCRDPaymentPage(Cart cart, Map creditCardConfig,  configs, Closure renderer) {
        Map formModel = securePayService.securepaySecureFrame(configs, cart)
        String log = "Action: Redirecting to SecurePay\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "SecurePayLogger")
        renderer(view: "/plugins/securepay_payment_gateway/securepayHostedForm", model: formModel)
    }

    private void renderAPISECUREPAYCRDPaymentPage(Cart cart, Map creditCardConfig, Map configs, Closure renderer) {
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig,
                view: "/plugins/securepay_payment_gateway/direct.gsp"])
    }

    PaymentInfo processSECUREPAYCRDAPIPayment(CardInfo cardInfo, Payment payment) {
        Integer expiryYear = cardInfo.expiryYear.toInteger();
        Integer expiryMonth = cardInfo.expiryMonth.toInteger();
        cardInfo.expiryYear = (expiryYear % 1000).toString()
        if(expiryMonth < 10) {
            cardInfo.expiryMonth = "0" + expiryMonth
        }
        return securePayService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }

    void processSECUREPAYWalletPayment(Map params, Closure redirect) {
        redirect(controller: "securePay", action: "processWalletPayment", params: params)
    }
}

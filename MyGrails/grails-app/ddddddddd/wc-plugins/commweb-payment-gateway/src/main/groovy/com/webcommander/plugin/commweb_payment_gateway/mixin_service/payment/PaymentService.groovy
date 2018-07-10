package com.webcommander.plugin.commweb_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.commweb_payment_gateway.CommwebService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession


class PaymentService {
    static CommwebService _commwebService

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }

    static getCommwebService() {
        if(_commwebService) {
            return _commwebService
        }
        return _commwebService = Holders.grailsApplication.mainContext.getBean(CommwebService)
    }

    void renderCOMMWEBCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.COMMWEB)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        "render${configMap["type"]}COMMWEBCRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderserverCOMMWEBCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address
        Map formModel = commwebService.processNetPayment(configs, address, cart)
        String log = "Action: Redirecting to Commweb\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "CommwebLogger")
        renderer(view: "/plugins/commweb_payment_gateway/commwebHostedForm", model: formModel)
    }

    private void rendermerchantCOMMWEBCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, view: "/plugins/commweb_payment_gateway/direct.gsp"])
    }

    PaymentInfo processCOMMWEBCRDAPIPayment(CardInfo cardInfo, Payment payment) {
        Integer expiryYear = cardInfo.expiryYear.toInteger();
        Integer expiryMonth = cardInfo.expiryMonth.toInteger();
        cardInfo.expiryYear = (expiryYear % 1000).toString()
        if(expiryMonth < 10) {
            cardInfo.expiryMonth = "0" + expiryMonth
        }
        return commwebService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }


}

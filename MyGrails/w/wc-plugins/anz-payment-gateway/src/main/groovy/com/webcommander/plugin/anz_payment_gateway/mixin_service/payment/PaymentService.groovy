package com.webcommander.plugin.anz_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.anz_payment_gateway.AnzService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession


class PaymentService {
    static AnzService _anzService
    static ApplicationTagLib _app

    static getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    static getAnzService() {
        return _anzService ?: (_anzService = Holders.grailsApplication.mainContext.getBean(AnzService))
    }

    void renderANZCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.ANZ)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        "render${configMap["type"]}ANZCRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderNETANZCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address
        Map formModel = anzService.processNetPayment(configs, address, cart)
        String log = "Action: Redirecting to Anz\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "AnzLogger")
        renderer(view: "/plugins/anz_payment_gateway/anzHostedForm", model: formModel)
    }

    private void renderAPIANZCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, view: "/plugins/anz_payment_gateway/direct.gsp"])
    }

    PaymentInfo processANZCRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return anzService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }

}

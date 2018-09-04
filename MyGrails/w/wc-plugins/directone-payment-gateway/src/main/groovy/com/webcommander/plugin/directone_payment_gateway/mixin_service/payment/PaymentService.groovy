package com.webcommander.plugin.directone_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.directone_payment_gateway.DirectOneService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession

class PaymentService {
    static DirectOneService _directOneService

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }

    static getDirectOneService() {
        if(_directOneService) {
            return _directOneService
        }
        return _directOneService = Holders.grailsApplication.mainContext.getBean(DirectOneService)
    }

    void renderDIRECTONECRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.DIRECTONE)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        "render${configMap["type"]}DIRECTONECRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderNETDIRECTONECRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address
        Map formModel = directOneService.hostedPayment(configs, address, cart)
        String log = "Action: Redirecting to DirectOne\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "DirectOneLogger")
        renderer(view: "/plugins/directone_payment_gateway/directOneHostedForm", model: formModel)
    }

    private void renderAPIDIRECTONECRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig,
                view: "/plugins/directone_payment_gateway/direct.gsp"])
    }

    PaymentInfo processDIRECTONECRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return directOneService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }

}

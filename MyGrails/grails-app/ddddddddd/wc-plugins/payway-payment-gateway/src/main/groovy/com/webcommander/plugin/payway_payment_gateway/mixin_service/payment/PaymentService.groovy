package com.webcommander.plugin.payway_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.payway_payment_gateway.PayWayPaymentService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession

/**
 * Created by zobair on 08/02/14.*/
class PaymentService {
    static PayWayPaymentService _payWayPaymentService;

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }
    static getPayWayPaymentService() {
        if(_payWayPaymentService) {
            return _payWayPaymentService
        }
        return _payWayPaymentService = Holders.grailsApplication.mainContext.getBean(PayWayPaymentService)
    }

    void renderPAYWAYCRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.PAYWAY)
        Map configMap = [:];
        configs.each {
            configMap[it.name] = it.value;
        }
        "render${configMap["type"]}PAYWAYCRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderNETPAYWAYCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        HttpSession session = AppUtil.session
        AddressData address = session.effective_billing_address;
        Map formModel = payWayPaymentService.paywayHostedToken(configs, address, cart);
        String log = "Action: Redirecting to Payway\nRequestData:\n${AppUtil.getQueryStringFromMap(formModel)}\n"
        WcLogManager.log(log, "PaywayLogger")
        renderer(view: "/plugins/payway_payment_gateway/paywayHostedForm", model: formModel)

    }

    private void renderAPIPAYWAYCRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, view: "/plugins/payway_payment_gateway/direct.gsp"])
    }

    PaymentInfo processPAYWAYCRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return payWayPaymentService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }

    void processPAYWAYWalletPayment(Map params, Closure redirect) {
        redirect(controller: "payWay", action: "processWalletPayment", params: params)
    }
}

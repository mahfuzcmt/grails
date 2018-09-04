package com.webcommander.plugin.square_payment_gateway.mixin_service.payment

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.AddressData
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.square_payment_gateway.SquareService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

import javax.servlet.http.HttpSession

class PaymentService {

    static SquareService _squareService

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }
    static getSquareService() {
        if(_squareService) {
            return _squareService
        }
        return _squareService = Holders.grailsApplication.mainContext.getBean(SquareService)
    }

    void renderSQUARECRDPaymentPage(Cart cart, Closure renderer) {
        def configs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE)
        Map configMap = [:]
        configs.each {
            configMap[it.name] = it.value;
        }
        "render${configMap["type"]}SQUARECRDPaymentPage"(cart, configMap, renderer)
    }

    private void renderserverSQUARECRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        def checkoutUrl = squareService.processHostedPayment()
        renderer(view: "/plugins/square_payment_gateway/site/squareHostedForm", model: [checkoutUrl: checkoutUrl])
    }

    private void rendermerchantSQUARECRDPaymentPage(Cart cart, Map configs, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        def squareConfigs = PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.SQUARE)
        Map squareConfigMap = [:]
        squareConfigs.each {
            squareConfigMap[it.name] = it.value;
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, squareConfig: squareConfigMap, view: "/plugins/square_payment_gateway/site/paymentPage.gsp"])
    }
}

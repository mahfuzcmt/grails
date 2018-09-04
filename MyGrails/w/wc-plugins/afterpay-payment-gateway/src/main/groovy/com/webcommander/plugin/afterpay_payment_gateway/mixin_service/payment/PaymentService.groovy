package com.webcommander.plugin.afterpay_payment_gateway.mixin_service.payment

import grails.util.Holders
import com.webcommander.models.Cart
import com.webcommander.ApplicationTagLib
import com.webcommander.plugin.afterpay_payment_gateway.AfterpayService

class PaymentService {
    static AfterpayService _afterpayService
    static ApplicationTagLib _app

    static getApp() {
        return _app ?: (_app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    static getAfterpayService() {
        return _afterpayService ?: (_afterpayService = Holders.grailsApplication.mainContext.getBean(AfterpayService))
    }

    void renderAPYPaymentPage(Cart cart, Closure renderer) {
        Map apiResponse = afterpayService.getToken(cart)
        String token = apiResponse.token
        renderer(view: "/site/siteAutoPage", model: [name: "afterpay.payment.gateway",token: token, view: "/plugins/afterpay_payment_gateway/direct.gsp", config: afterpayService.getAfterpayConfig()])
    }

}

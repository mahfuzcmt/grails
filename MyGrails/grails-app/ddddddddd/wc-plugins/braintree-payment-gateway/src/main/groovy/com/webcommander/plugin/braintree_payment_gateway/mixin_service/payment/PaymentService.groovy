package com.webcommander.plugin.braintree_payment_gateway.mixin_service.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.braintree_payment_gateway.BrainTreeService
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders

class PaymentService {
    static BrainTreeService _brainTreeService

    static getBrainTreeService() {
        if(_brainTreeService) {
            return _brainTreeService
        }
        return _brainTreeService = Holders.grailsApplication.mainContext.getBean(BrainTreeService)
    }

    void renderBRAINTREECRDPaymentPage(Cart cart, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        String clientToken = brainTreeService.gateway.clientToken().generate()
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, clientToken: clientToken, view: "/plugins/braintree_payment_gateway/direct.gsp"])
    }

    PaymentInfo processBRAINTREECRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return brainTreeService.processApiPayment(cardInfo, payment.amount, payment.id)
    }
}

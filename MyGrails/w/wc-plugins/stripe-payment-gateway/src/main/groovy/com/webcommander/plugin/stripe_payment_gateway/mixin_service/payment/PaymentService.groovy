package com.webcommander.plugin.stripe_payment_gateway.mixin_service.payment


import com.webcommander.constants.DomainConstants
import com.webcommander.models.CardInfo
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.stripe_payment_gateway.StripeService
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGatewayMeta
import grails.util.Holders



class PaymentService {
    private static StripeService _stripeService
    private static StripeService getStripeService() {
        return _stripeService ?: (_stripeService = Holders.grailsApplication.mainContext.getBean(StripeService))
    }

    void renderSTRIPECRDPaymentPage(Cart cart, Closure renderer) {
        Map creditCardConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD).each {
            creditCardConfig[it.name] = it.value;
        }
        Map stripeConfig = [:]
        PaymentGatewayMeta.findAllByFieldFor(DomainConstants.CARD_PAYMENT_PROCESSOR_CODE.STRIPE).each {
            stripeConfig[it.name] = it.value
        }
        renderer(view: "/site/siteAutoPage", model: [name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_PAGE, creditCardConfig: creditCardConfig, stripeConfig: stripeConfig, view: "/plugins/stripe_payment_gateway/direct.gsp"])
    }

    PaymentInfo processSTRIPECRDAPIPayment(CardInfo cardInfo, Payment payment) {
        return stripeService.processApiPayment(cardInfo, payment.amount, payment.order.id, payment.id)
    }

}

package com.webcommander.controllers.site

import com.webcommander.authentication.annotations.AutoGeneratedPage
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayService

class PaymentController {

    PaymentService paymentService
    PaymentGatewayService paymentGatewayService

    @AutoGeneratedPage("post.payment")
    def success() {
        Payment payment;
        if (params.paymentInstance) { // sending it will not update payment status
            payment = params.paymentInstance
        } else if (params.payments && params.payments instanceof List<PaymentInfo>) {
            List<PaymentInfo> payments = params.payments
            paymentService.processPostPayment(payments)
            payment = Payment.get(payments.last().paymentRef)
        } else {
            payment = paymentService.processPostPayment(params.payment, params.pending ? DomainConstants.PAYMENT_STATUS.PENDING : DomainConstants.PAYMENT_STATUS.SUCCESS)
        }
        if (!payment) {
            redirect url: "/"
            return;
        }
        PaymentGateway gateway = paymentGatewayService.getPaymentGatewayByCode(payment.gatewayCode);
        StoreDetail storeDetail = StoreDetail.first();
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL);
        String view = "/site/siteAutoPage";
        if(payment.id) {
            payment = payment.attach()
        }
        CartManager.removeCart(session.id)

        if((AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model") == "true") && session.customer) {
            Cart cacheCart = CartManager.hasCart(session.customer.encodeAsMD5()) ? CartManager.getCart(session.customer.encodeAsMD5(), false) : null
            if(cacheCart) {
                cacheCart.cartItemList.size() ? CartManager.addToCartByItems(cacheCart.cartItemList) : ""
                CartManager.removeCart(session.customer.encodeAsMD5())
            }
        }

        Map model = [
            name: DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_SUCCESS_PAGE, order: Order.get(payment.orderId),
            payment: payment, storeDetail: storeDetail, gateway: gateway,
            config: config, view: "site/autopage/paymentSuccess.gsp"
        ];
        view = HookManager.hook("auto-page-view-model", view, model);
        render (view: view, model: model)
    }

    def failed() {
        if (!params.paymentInstance) {
            Payment payment = paymentService.processPostPayment(params.payment, DomainConstants.PAYMENT_STATUS.FAILED)
            if (!payment) {
                redirect url: "/"
                return;
            }
        }
        flash.model = [error: params.error ? g.message(code: params.error) : g.message(code: "could.not.process.payment.contact.with.vendor")]
        flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
        redirect(controller: "shop", action: "checkout")
    }

    def cancelled() {
        Payment payment = paymentService.processPostPayment(params.payment, DomainConstants.PAYMENT_STATUS.CANCELLED)
        if (!payment) {
            redirect url: "/"
            return;
        }
        flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
        redirect(controller: "shop", action: "checkout")
    }

}

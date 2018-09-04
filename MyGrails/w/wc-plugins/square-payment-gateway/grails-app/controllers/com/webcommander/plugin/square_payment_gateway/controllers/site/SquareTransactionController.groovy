package com.webcommander.plugin.square_payment_gateway.controllers.site

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.square_payment_gateway.SquareService
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class SquareTransactionController {

    SquareService squareService

    private void doLog(Map params) {
        String log = "Action: IPN from Square\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "SquareLogger")
    }

    def processApiPayment(){
        try {
            PaymentInfo info = squareService.processApiPayment(params.nonce)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            String errorMessage = t instanceof PaymentGatewayException ? t.getMessage() : g.message(code: "could.not.process.payment.contact.with.vendor")
            flash.model = [error: errorMessage]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }

    def processHostedPayment(){
        def checkoutUrl = squareService.processHostedPayment()
        render(view: "/plugins/square_payment_gateway/site/squareHostedForm", model: [checkoutUrl: checkoutUrl])
    }

    def completeCheckout() {
        doLog(params)
        Long paymentId = params["referenceId"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            redirect(url: "/")
            return
        }
        PaymentInfo info = squareService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

}

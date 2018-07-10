package com.webcommander.plugin.commweb_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.commweb_payment_gateway.CommwebService
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class CommwebController {
    CommwebService commwebService

    private void doLog(Map params) {
        String log = "Action: IPN from Commweb\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "CommwebLogger")
    }
    def paymentReturn() {
        doLog(params)
        Long paymentId = params["vpc_MerchTxnRef"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = commwebService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def processApiPayment() {
        try {
            PaymentInfo info = commwebService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            String errorMessage = t instanceof PaymentGatewayException ? t.getMessage() : g.message(code: "could.not.process.payment.contact.with.vendor")
            flash.model = [error: errorMessage]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

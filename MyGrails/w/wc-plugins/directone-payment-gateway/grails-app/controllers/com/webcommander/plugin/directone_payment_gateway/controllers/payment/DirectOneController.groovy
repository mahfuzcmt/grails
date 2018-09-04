package com.webcommander.plugin.directone_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.directone_payment_gateway.DirectOneService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class DirectOneController {
    DirectOneService directOneService
    PaymentService paymentService

    private void doLog(Map params) {
        String log = "Action: IPN from DirectOne\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "DirectOneLogger")
    }
    def paymentReturn() {
        doLog(params)
        Long paymentId = params["payment_id"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = directOneService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def paymentNotify() {
        doLog(params)
        Long paymentId = params["payment_id"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            render text: "invalid"
            return
        }
        PaymentInfo info = directOneService.resolveInfo(params)
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING) {
            paymentService.processPostPayment(info, info.success ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED)
            render text: "processed"
        } else {
            render text: "duplicate"
        }
    }

    def processApiPayment() {
        try {
            PaymentInfo info = directOneService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            log.error("Error Processing Payment - " + t)
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

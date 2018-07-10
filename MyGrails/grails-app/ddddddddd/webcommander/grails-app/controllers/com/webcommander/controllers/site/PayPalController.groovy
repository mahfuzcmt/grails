package com.webcommander.controllers.site

import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PayPalService
import com.webcommander.webcommerce.Payment

class PayPalController {
    PaymentService paymentService
    PayPalService payPalService

    private void doLog(Map params) {
        String log = "\nAction: IPN from PayPal\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "PayPalLogger")
    }

    def returnPayment() {
        doLog(params)
        LinkedHashMap paramMap = new LinkedHashMap(params);
        paramMap.remove("controller");
        paramMap.remove("action");
        Long paymentId = params.long("custom")
        Payment payment = Payment.get(paymentId);
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = payPalService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING) {
            Boolean isValid = payPalService.validatePayment(paramMap);
            if (!isValid) {
                redirect(url: "/")
                return;
            }
        } else {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info;
        if(params.payment_status == "Pending") {
            flashMap.pending = true;
        }
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def notifyPayment() {
        doLog(params)
        LinkedHashMap paramMap = new LinkedHashMap(params);
        paramMap.remove("controller");
        paramMap.remove("action");
        Long paymentId = params.long("custom")
        Payment payment = Payment.get(paymentId);
        if(!payment) {
            render text: "invalid"
            return;
        }
        PaymentInfo info = payPalService.resolveInfo(params)
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING || payment.status == DomainConstants.PAYMENT_STATUS.PENDING) {
            Boolean isValid = payPalService.validatePayment(paramMap);
            if (!isValid) {
                render text: "invalid"
                return;
            }
            if(payment.status == DomainConstants.PAYMENT_STATUS.PENDING && !paymentService.hasPaymentEntry(paymentId)) {
                paymentService.storePaymentEntry(paymentId)
            }
            paymentService.processPostPayment(info, info.success ? (params.payment_status == "Completed" ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.PENDING) :
                    DomainConstants.PAYMENT_STATUS.FAILED)
            render text: "processed"
        } else {
            render text: "duplicate"
        }
    }

    def cancelPayment() {
        doLog(params)
        PaymentInfo info = payPalService.resolveInfo(params)
        flash.param = [payment: info]
        redirect(controller: "payment", action: "cancelled")
    }
}

package com.webcommander.plugin.afterpay_payment_gateway.controllers


import com.webcommander.plugin.afterpay_payment_gateway.AfterpayService
import com.webcommander.util.AppUtil
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.webcommerce.Payment
import com.webcommander.constants.DomainConstants

class AfterpayController {

    AfterpayService afterpayService

    private void doLog(Map params) {
        String log = "Action: Afterpay Response Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "AfterpayLogger")
    }

    def confirmPayment() {
        doLog(params)
        Map response = afterpayService.getOrder(params)
        if (!response) {
            redirect(url: "/")
            return;
        }
        Long paymentId  = Long.parseLong(response.merchantReference)
        Payment payment = Payment.get(paymentId);
        if(!payment) {
            redirect(url: "/")
            return;
        }
        def paymentData = afterpayService.capturePayment(response, params)
        PaymentInfo info = afterpayService.resolveConfirmInfo(paymentData, params)
        Map flashMap = [:]
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.pending = false;
        } else {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info;
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def cancelPayment() {
        doLog(params)
        if(!params.orderToken) {
            redirect(url: "/")
            return;
        }
        Map response = afterpayService.getOrder(params)
        PaymentInfo info = afterpayService.resolveCancelInfo(response, params)
        flash.param = [payment: info]
        redirect(controller: "payment", action: "cancelled")
    }

}

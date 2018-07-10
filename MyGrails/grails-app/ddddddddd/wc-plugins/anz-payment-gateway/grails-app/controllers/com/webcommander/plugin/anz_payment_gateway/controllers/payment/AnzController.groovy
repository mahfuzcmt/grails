package com.webcommander.plugin.anz_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.anz_payment_gateway.AnzService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class AnzController {
    AnzService anzService

    private void doLog(Map params) {
        String log = "Action: IPN from Anz\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "AnzLogger")
    }
    def paymentReturn() {
        doLog(params)
        Long paymentId = params["vpc_MerchTxnRef"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = anzService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def paymentCancel() {

    }

    def processApiPayment() {
        try {
            PaymentInfo info = anzService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

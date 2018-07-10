package com.webcommander.plugin.epath_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.epath_payment_gateway.EpathService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class EpathController {
    EpathService epathService
    PaymentService paymentService

    private void doLog(Map params) {
        String log = "Action: IPN from E-path\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "E-pathLogger")
    }
    def paymentReturn() {
        doLog(params)
        Long paymentId = params["opt"].toLong()
        Payment payment = Payment.get(paymentId)
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = epathService.resolveInfo(params)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) {
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }
}

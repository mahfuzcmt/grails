package com.webcommander.plugin.eway_payment_gateway.controllers.payment

import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.eway_payment_gateway.EwayPaymentService
import com.webcommander.util.AppUtil

class EwayController {

    EwayPaymentService ewayPaymentService

    private void doLog(Map params) {
        String log = "Action: IPN from Eway\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "EwayLogger")
    }
    def cancelPayment() {
        doLog(params)
        if(!params.AccessPaymentCode) {
            redirect(uri: "/")
            return;
        }
        PaymentInfo info = ewayPaymentService.resolveInfo(params.AccessPaymentCode)
        flash.param = [payment: info]
        redirect(controller: "payment", action: "cancelled")
    }

    def returnPayment() {
        doLog(params)
        if(!params.AccessPaymentCode) {
            redirect(uri: "/")
            return;
        }
        PaymentInfo info = ewayPaymentService.resolveInfo(params.AccessPaymentCode)
        flash.param = [payment: info]
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def processApiPayment() {
        try {
            PaymentInfo info = ewayPaymentService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }

    def processWalletPayment() {
        try {
            PaymentInfo info = ewayPaymentService.processWalletPayment(params)
            session.removeAttribute("payment_wallet")
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch(Throwable t) {
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

package com.webcommander.plugin.securepay_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.securepay_payment_gateway.SecurePayService
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class SecurePayController {
    SecurePayService securePayService
    PaymentService paymentService

    private void doLog(Map params) {
        String log = "Action: IPN from SecurePay\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "SecurePayLogger")
    }
    def paymentReturn() {
        doLog(params)
        Long paymentId = params["refid"].toLong()
        Payment payment = Payment.get(paymentId)
        PaymentInfo info
        if(!payment || (info = securePayService.resolveInfo(params)) == null) {
            redirect(url: "/")
            return;
        }
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
        Long paymentId = params["refid"].toLong()
        Payment payment = Payment.get(paymentId)
        PaymentInfo info
        if(!payment || (info = securePayService.resolveInfo(params)) == null) {
            render text: "invalid"
            return
        }
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING || payment.status == DomainConstants.PAYMENT_STATUS.PENDING) {
            if(payment.status == DomainConstants.PAYMENT_STATUS.PENDING && !paymentService.hasPaymentEntry(paymentId)) {
                paymentService.storePaymentEntry(paymentId)
            }
            paymentService.processPostPayment(info, info.success ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED)
            render text: "processed"
        } else {
            render text: "duplicate"
        }
    }

    def processApiPayment() {
        try {
            PaymentInfo info = securePayService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch (PaymentGatewayException ex) {
            paymentService.processPostPayment(ex.paymentInfo, DomainConstants.PAYMENT_STATUS.FAILED)
            flash.model = [error: ex.message]
            flash.param = [confirmed: true]
            redirect(controller: "shop", action: "payment")
        } catch(Throwable t) {
            log.error(t.message, t)
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }

    def processWalletPayment() {
        try {
            PaymentInfo info = securePayService.processWalletPayment(params)
            session.removeAttribute("payment_wallet")
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch (PaymentGatewayException ex) {
            paymentService.processPostPayment(ex.paymentInfo, DomainConstants.PAYMENT_STATUS.FAILED)
            flash.model = [error: ex.message]
            flash.param = [confirmed: true]
            redirect(controller: "shop", action: "payment")
        } catch(Throwable t) {
            log.error(t.message, t)
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

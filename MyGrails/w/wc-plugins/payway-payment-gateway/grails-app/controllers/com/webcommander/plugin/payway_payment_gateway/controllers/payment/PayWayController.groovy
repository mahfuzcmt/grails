package com.webcommander.plugin.payway_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.log.WcLogManager
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.PaymentService
import com.webcommander.plugin.payway_payment_gateway.PayWayPaymentService
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Payment

class PayWayController {
    PayWayPaymentService payWayPaymentService
    PaymentService paymentService

    private void doLog(Map params) {
        String log = "Action: IPN from PayWay\nResponse Data:\n${AppUtil.getQueryStringFromMap(params)}"
        WcLogManager.log(log, "PayWayLogger")
    }
    def paymentReturn() {
        doLog(params)
        Map decryptedParams = payWayPaymentService.decryptNetReturnData(params.EncryptedParameters, params.Signature)
        Long paymentId = decryptedParams.paymentId.toLong()
        Payment payment = Payment.get(paymentId);
        if(!payment) {
            redirect(url: "/")
            return;
        }
        PaymentInfo info = payWayPaymentService.resolveInfoFromReturn(decryptedParams)
        Map flashMap = [:]
        if(payment.status != DomainConstants.PAYMENT_STATUS.AWAITING) { //checking if already processed by notificarion
            flashMap.paymentInstance = payment
        }
        flashMap.payment = info;
        flash.param = flashMap
        redirect(controller: "payment", action: info.success ? "success" : "failed")
    }

    def paymentNotify() {
        doLog(params)
        Long paymentId = params.long("paymentId");
        Payment payment = Payment.get(paymentId);
        if(!payment) {
            render text: "invalid"
            return;
        }
        payWayPaymentService.verifyNotification(params.username, params.password)
        PaymentInfo info = payWayPaymentService.resolveInfoFromNotification(params)
        if(payment.status == DomainConstants.PAYMENT_STATUS.AWAITING) {
            paymentService.processPostPayment(info, info.success ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED)
            render text: "processed"
        } else {
            render text: "duplicate"
        }
    }

    def processApiPayment() {
        try {
            PaymentInfo info = payWayPaymentService.processApiPayment(params)
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
            PaymentInfo info = payWayPaymentService.processWalletPayment(params)
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

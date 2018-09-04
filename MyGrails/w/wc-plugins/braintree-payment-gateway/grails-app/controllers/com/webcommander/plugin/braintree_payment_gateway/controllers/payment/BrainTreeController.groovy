package com.webcommander.plugin.braintree_payment_gateway.controllers.payment

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.PaymentInfo
import com.webcommander.plugin.braintree_payment_gateway.BrainTreeService
import com.webcommander.payment.PaymentService
import com.webcommander.throwables.PaymentGatewayException

class BrainTreeController {
    BrainTreeService brainTreeService
    PaymentService paymentService

    def processApiPayment() {
        try {
            PaymentInfo info = brainTreeService.processApiPayment(params)
            flash.param = [payment: info]
            redirect(controller: "payment", action: info.success ? "success" : "failed")
        } catch (PaymentGatewayException ex) {
            paymentService.processPostPayment(ex.paymentInfo, DomainConstants.PAYMENT_STATUS.FAILED)
            flash.model = [error: ex.message]
            flash.param = [confirmed: true]
            redirect(controller: "shop", action: "payment")
        }  catch(Throwable t) {
            flash.model = [error: g.message(code: "could.not.process.payment.contact.with.vendor")]
            flash.param = [step: NamedConstants.CHECKOUT_PAGE_STEP.PAYMENT_METHOD]
            redirect(controller: "shop", action: "checkout")
        }
    }
}

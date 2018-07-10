package com.webcommander.throwables

import com.webcommander.models.PaymentInfo
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

class PaymentGatewayException extends RuntimeException {
    private List messageParams;
    PaymentInfo paymentInfo
    private static ApplicationTagLib _g

    private static ApplicationTagLib getG() {
        _g ?: (_g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }

    public PaymentGatewayException() {}

    public PaymentGatewayException(String messageCode) {
        super(messageCode)
    }

    public PaymentGatewayException(String messageCode, List params) {
        super(messageCode)
        messageParams = params;
    }

    public PaymentGatewayException(String messageCode, List params, PaymentInfo paymentInfo) {
        super(messageCode)
        messageParams = params;
        this.paymentInfo = paymentInfo
    }

    public String getMessage() {
        g.message(code: super.getMessage(), args: messageParams)
    }


}
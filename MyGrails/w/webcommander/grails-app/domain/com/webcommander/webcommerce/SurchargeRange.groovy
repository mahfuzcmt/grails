package com.webcommander.webcommerce

class SurchargeRange {

    Long id

    Double orderAmountFrom
    Double orderAmountTo
    Double surchargeAmount

    static belongsTo = [
        paymentGateway: PaymentGateway
    ]

    def boolean equals(Object obj) {
        if(obj instanceof SurchargeRange)
            return id == obj.id;
        return false;
    }

    def int hashCode() {
        return id == null ? super.hashCode() : ("" + id).hashCode();
    }
}

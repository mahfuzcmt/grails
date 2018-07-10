package com.webcommander.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.util.Base64Coder

class CreditCard {

    Long id
    Date created
    Date updated

    String cardHolderName
    String cardName
    String cardType
    String cardNumber
    String cardMonth
    String cardYear
    String gatewayName
    String gatewayToken

    Customer customer

    Boolean isActive = true
    Boolean isDefault = false
    Boolean isExpired = false

    Long paymentCount = 0L
    Double paymentTotal = 0D

    static belongsTo = [customer: Customer]

    static constraints = {
        cardName(nullable: true)
    }

    static transients = ['plusPaymentCount', 'plusPaymentTotal', 'getToken']

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    void plusPaymentCount() {
        this.paymentCount = this.paymentCount + 1
    }

    void plusPaymentTotal(Double amount) {
        this.paymentTotal = this.paymentTotal + amount
    }

    String getToken() {
        return new String(Base64Coder.decode(this.gatewayToken))
    }
}

package com.webcommander.webcommerce

class Payment {

    Long id

    Double amount
    Double surcharge = 0

    String trackInfo
    String payerInfo
    String gatewayCode
    String gatewayResponse
    String status // awaiting, refunded, failed, success

    Date payingDate
    Date created
    Date updated

    Order order

    static constraints = {
        trackInfo(nullable: true)
        payerInfo(nullable: true)
        gatewayResponse(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }
}

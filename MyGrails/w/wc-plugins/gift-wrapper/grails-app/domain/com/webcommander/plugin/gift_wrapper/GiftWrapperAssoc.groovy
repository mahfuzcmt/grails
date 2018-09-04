package com.webcommander.plugin.gift_wrapper

class GiftWrapperAssoc {
    Long id

    Long giftWrapperId
    Long productId
    Long assocItemId
    String assocType
    Long assocTypeId

    String giftWrapperName
    Double giftWrapperPrice
    String message
    Double price = 0
    Double tax = 0

    Date created
    Date updated

    static constraints = {
        message(nullable: true)
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

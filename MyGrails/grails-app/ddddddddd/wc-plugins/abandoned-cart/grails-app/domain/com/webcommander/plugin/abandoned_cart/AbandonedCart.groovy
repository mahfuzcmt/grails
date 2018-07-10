package com.webcommander.plugin.abandoned_cart

import com.webcommander.admin.Customer

class AbandonedCart {

    Long id
    Customer customer
    String notificationStatus = "pending" //pending, disabled, sent
    Integer notificationSentCount = 0
    Collection<AbandonedCartItem> cartItems = []

    Date created

    static hasMany = [cartItems: AbandonedCartItem]
    static transients = ['total', 'discount', 'tax']

    static constraints = {
        customer(nullable: true)
        cartItems(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }

    Double total() {
        Double total = 0;
        cartItems.each {
            Map priceObject = it.priceObject();
            if(priceObject) {
                total += priceObject.total
            }
        }
        return total
    }
    Double discount() {
        Double discount = 0;
        cartItems.each {
            Map priceObject = it.priceObject();
            if(priceObject) {
                discount += priceObject.discount
            }
        }
        return discount
    }
    Double tax() {
        Double tax = 0;
        cartItems.each {
            Map priceObject = it.priceObject();
            if(priceObject) {
                tax += priceObject.tax
            }
        }
        return tax
    }
}

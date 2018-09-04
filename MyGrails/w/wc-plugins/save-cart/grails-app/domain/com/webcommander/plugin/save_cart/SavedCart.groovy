package com.webcommander.plugin.save_cart

import com.webcommander.admin.Customer

class SavedCart {
    Long id
    String name
    Customer customer

    Date created
    Date updated

    Collection<SavedCartItem> cartItems = []
    static hasMany = [cartItems: SavedCartItem]

    static constraints = {
    }

    static transients = ['total', 'discount', 'tax']

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

    Double getTotal() {
        return cartItems.sum { it.total }
    }

    Double getDiscount() {
        return cartItems.sum { it.discount }
    }

    Double getTax() {
        return cartItems.sum { it.tax }
    }
}

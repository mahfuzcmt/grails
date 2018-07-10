package com.webcommander.webcommerce

class CombinedProduct {

    Long id
    String label
    Product baseProduct
    Product includedProduct
    Integer quantity
    Double price

    Date created
    Date updated

    static constraints = {
        price(nullable: true)
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

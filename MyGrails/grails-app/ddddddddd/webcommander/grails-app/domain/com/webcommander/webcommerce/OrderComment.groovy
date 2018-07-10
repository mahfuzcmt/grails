package com.webcommander.webcommerce

class OrderComment {

    Long id
    String content
    String adminName
    Boolean isVisibleToCustomer = true
    Boolean isAdmin = true
    Order order

    Date created

    static belongsTo = [order: Order]

    static constraints = {
        content(maxSize: 2000)
        adminName(nullable: true)
    }

    static mapping = {
        content type: "text"
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }
}

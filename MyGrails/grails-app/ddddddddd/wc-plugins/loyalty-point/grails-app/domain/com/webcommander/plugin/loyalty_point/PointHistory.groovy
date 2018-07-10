package com.webcommander.plugin.loyalty_point

import com.webcommander.admin.Customer
import com.webcommander.webcommerce.Order

class PointHistory {
    Long id

    Customer customer
    Order order
    Long pointCredited = 0
    Long pointDebited = 0
    String comment
    String type

    Date created

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }

    static constraints = {
        comment(nullable: true)
        order(nullable: true)
    }
}

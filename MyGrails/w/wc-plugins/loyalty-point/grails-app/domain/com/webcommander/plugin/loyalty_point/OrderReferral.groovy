package com.webcommander.plugin.loyalty_point

import com.webcommander.webcommerce.Order

class OrderReferral {

    Long id
    Order order
    String referralCode

    static constraints = {
        order(nullable: false, unique: true)
        referralCode(nullable: false)
    }
}

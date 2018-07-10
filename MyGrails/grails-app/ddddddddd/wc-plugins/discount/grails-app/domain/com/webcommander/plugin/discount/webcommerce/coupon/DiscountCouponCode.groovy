package com.webcommander.plugin.discount.webcommerce.coupon

import com.webcommander.admin.Customer

/**
 * Created by sharif ul islam on 13/03/2018.
 */
class DiscountCouponCode {

    Long id

    String code

    Boolean isActive = true

    Customer customer

    static belongsTo = [assoc: DiscountCouponAssoc]

    static clone_exclude = ["assoc"]

    static copy_reference = [
            "customer"
    ]

    static constraints = {
        code(unique: true, blank: false)

        customer(nullable: true)
        assoc(nullable: true)
    }

}

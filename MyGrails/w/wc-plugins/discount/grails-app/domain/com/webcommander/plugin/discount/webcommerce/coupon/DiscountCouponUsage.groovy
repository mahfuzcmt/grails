package com.webcommander.plugin.discount.webcommerce.coupon

/**
 * Created by sharif ul islam on 13/03/2018.
 */
class DiscountCouponUsage {

    Long id

    Long orderId
    Long itemId
    Long customerId

    Double amount

    Date created

    static belongsTo = [coupon: DiscountCoupon]

    static constraints = {
        customerId(nullable: true)
        itemId(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }
}

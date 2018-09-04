package com.webcommander.plugin.discount.webcommerce.coupon

import com.webcommander.admin.Operator

/**
 * Created by sharif ul islam on 13/03/2018.
 */
class DiscountCoupon {

    Long id

    Boolean isActive = true

    Double discountAmount

    Date created

    DiscountCouponCode code
    DiscountCouponAssoc assoc
    Operator createdBy

    Collection<DiscountCouponUsage> usage = []

    static hasMany = [usage: DiscountCouponUsage]

    static clone_exclude = ["usage", "assoc", "code"]

    static constraints = {
        createdBy(nullable: true)
        discountAmount(nullable: true)
        created(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }

}

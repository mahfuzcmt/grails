package com.webcommander.plugin.discount.webcommerce

import com.webcommander.admin.Customer

class DiscountUsage {
    Long id

    Long orderId
    Long itemId
    Long customerId

    String appliedCouponCode

    Double amount

    Date created

    static belongsTo = [discount: CustomDiscount]

    static constraints = {
        customerId(nullable: true)
        itemId(nullable: true)
        appliedCouponCode(nullable: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }

    def getCustomer() {
        if (customerId) {
            return Customer.get(this.customerId)
        }
        return null
    }

}

package com.webcommander.plugin.discount.webcommerce.coupon

/**
 * Created by sharif ul islam on 13/03/2018.
 */
class DiscountCouponAssoc {

    Long id

    Boolean isUniqueCodeEachCustomer = false

    Collection<DiscountCouponCode> codes = []

    static copy_reference = ["codes"]

    static constraints = {

    }

    static hasMany = [codes: DiscountCouponCode]

    static mapping = {
        codes cache: true
    }

    def initiate() {
        codes.clear()
    }

}

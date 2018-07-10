package com.webcommander.plugin.discount.webcommerce.details

import com.webcommander.admin.Zone
import com.webcommander.plugin.discount.Constants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ShippingClass

class ShippingDiscountDetails {
    Long id
    String type
    String amountType
    String singleAmountType = Constants.AMOUNT_TYPE.FLAT

    Integer maximumTime

    Double singleAmount
    Double capAmount

    Zone zone
    ShippingClass shippingClass

    Collection<DiscountAmountTier> tiers = []

    static hasMany = [tiers: DiscountAmountTier]

    static constraints = {
        capAmount(nullable: true)
        singleAmount(nullable: true)
        maximumTime(nullable: true)
        zone(nullable: true)
        shippingClass(nullable: true)
        amountType(nullable: true)
    }

    String getDisplaySingleAmount() {
        return singleAmountType == Constants.AMOUNT_TYPE.FLAT ? AppUtil.baseCurrency.symbol + (singleAmount?.toConfigPrice() as String) : "$singleAmount%"
    }
}

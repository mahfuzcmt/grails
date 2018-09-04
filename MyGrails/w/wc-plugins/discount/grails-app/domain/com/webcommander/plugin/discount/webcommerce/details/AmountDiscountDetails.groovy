package com.webcommander.plugin.discount.webcommerce.details

import com.webcommander.plugin.discount.Constants
import com.webcommander.util.AppUtil

class AmountDiscountDetails {
    Long id
    String type = Constants.AMOUNT_DETAILS_TYPE.SINGLE
    Double singleAmount
    String singleAmountType = Constants.AMOUNT_TYPE.FLAT
    String minimumAmountOn = Constants.MINIMUM_AMOUNT_ON.EACH_ITEM

    Collection<DiscountAmountTier> tiers = []

    static hasMany = [tiers: DiscountAmountTier]

    static constraints = {
        singleAmount(nullable: true)
        minimumAmountOn(nullable: true)
    }

    String getDisplaySingleAmount() {
        return singleAmountType == Constants.AMOUNT_TYPE.FLAT ? AppUtil.baseCurrency.symbol + (singleAmount?.toConfigPrice() as String) : "$singleAmount%"
    }
}

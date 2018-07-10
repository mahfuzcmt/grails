package com.webcommander.plugin.discount.webcommerce.details

import com.webcommander.plugin.discount.Constants
import com.webcommander.util.AppUtil

class DiscountAmountTier {
    Long id

    Double minimumAmount
    Double amount
    String amountType = Constants.AMOUNT_TYPE.FLAT

    String getDisplayAmount() {
        return amountType == Constants.AMOUNT_TYPE.FLAT ? AppUtil.baseCurrency.symbol + (amount?.toConfigPrice() as String) : "$amount%"
    }
}

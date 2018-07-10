package com.webcommander.plugin.ebay_listing.admin.webmarketing

import com.webcommander.plugin.ebay_listing.constants.DomainConstants

class EbayPricingProfile {

    String type = DomainConstants.PRICING_PROFILE_TYPE.PROUDUCT_DEFAULT_PRICE
    String additionalType //$/%

    Double additionalAmount //delta amount
    Double newAmount

    static constraints = {
        additionalType(nullable: true)
        additionalAmount(nullable: true)
        newAmount(nullable: true)
    }

}

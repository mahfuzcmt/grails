package com.webcommander.plugin.ebay_listing.admin.webmarketing

import com.webcommander.plugin.ebay_listing.constants.DomainConstants

class EbayPricing {

    String type = DomainConstants.PRICING_TYPE.FIXED //auction/fixed
    String sellToQuantityType = DomainConstants.SELL_TO_QUANTITY_TYPE.JUST_ONE_ITEM;
    Integer quantity = 1
    Integer duration = 7 //1/3/5/7/10 days

    Boolean isPrivateListing = false

    EbayPricingProfile buyNowPrice
    EbayPricingProfile startingPrice

    static constraints = {
        buyNowPrice(nullable: true)
        startingPrice(nullable: true)
    }

}

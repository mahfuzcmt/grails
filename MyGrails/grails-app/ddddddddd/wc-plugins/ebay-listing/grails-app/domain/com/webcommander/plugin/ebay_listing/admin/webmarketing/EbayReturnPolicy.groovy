package com.webcommander.plugin.ebay_listing.admin.webmarketing

class EbayReturnPolicy {

    String returnWithin
    String refundType
    String returnShippingPaidBy
    String additionalReturnPolicyNote

    Boolean acceptReturn = false

    static constraints = {
        returnWithin(nullable: true)
        refundType(nullable: true)
        returnShippingPaidBy(nullable: true)
        additionalReturnPolicyNote(nullable: true)
    }

}

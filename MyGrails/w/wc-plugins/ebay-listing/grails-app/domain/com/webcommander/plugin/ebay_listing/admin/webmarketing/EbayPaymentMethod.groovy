package com.webcommander.plugin.ebay_listing.admin.webmarketing

class EbayPaymentMethod {

    String name
    Collection<EbayMetaValue> metaValues = []

    static hasMany = [metaValues: EbayMetaValue]
}

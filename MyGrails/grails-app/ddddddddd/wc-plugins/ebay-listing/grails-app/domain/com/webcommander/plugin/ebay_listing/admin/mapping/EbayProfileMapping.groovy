package com.webcommander.plugin.ebay_listing.admin.mapping

import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile
import com.webcommander.webcommerce.Product

class EbayProfileMapping {
    Long id
    Product product
    EbayListingProfile listingProfile

    static constraints = {
        product unique: true
    }

}

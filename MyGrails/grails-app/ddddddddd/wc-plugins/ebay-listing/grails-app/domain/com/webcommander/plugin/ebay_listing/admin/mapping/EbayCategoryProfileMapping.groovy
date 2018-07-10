package com.webcommander.plugin.ebay_listing.admin.mapping

import com.webcommander.plugin.ebay_listing.admin.webmarketing.EbayListingProfile
import com.webcommander.webcommerce.Category

class EbayCategoryProfileMapping {
    Long id
    Category category
    EbayListingProfile listingProfile

    static constraints = {
        category unique: true
    }

}

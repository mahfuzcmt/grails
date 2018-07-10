package com.webcommander.plugin.ebay_listing.admin.mapping

import com.webcommander.webcommerce.Product

class EbayItemMapping {
    Long id
    Product product
    String ebayItemId
    Integer sold = 0

    static constraints = {
    }
}

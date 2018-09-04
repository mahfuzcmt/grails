package com.webcommander.plugin.discount_per_product_variation

import com.webcommander.manager.HookManager
import com.webcommander.plugin.variation.VariationService

class DiscountProductVariationTagLib {
    static namespace = "discountProductVariation"

    VariationService variationService

    def productSelectionColumn = { Map attrs, body ->
        out << "<span class='product-name'>"
        out << body()
        Boolean isVariationExist = variationService.isVariationExist(attrs.productId as Long)
        if(isVariationExist) {
            out << "</span><span class='show-variation' product-id='${attrs.productId}'>${g.message(code: 'variation')}</span>"
        }
    }
}
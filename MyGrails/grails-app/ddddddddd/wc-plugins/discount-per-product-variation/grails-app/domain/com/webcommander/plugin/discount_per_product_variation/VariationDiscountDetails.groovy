package com.webcommander.plugin.discount_per_product_variation

import com.webcommander.plugin.discount_per_product_variation.helper.ProductWithVariation

class VariationDiscountDetails {
    Long discountId
    Collection<ProductWithVariation> productWithVariations = []

    static constraints = {
        discountId(nullable: false)
    }

    static hasMany = [
        productWithVariations: ProductWithVariation
    ]

    static mapping = {
//        productVariations fetch: 'join'
    }
}

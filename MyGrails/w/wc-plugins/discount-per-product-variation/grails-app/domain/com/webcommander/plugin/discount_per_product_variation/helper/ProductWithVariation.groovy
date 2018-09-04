package com.webcommander.plugin.discount_per_product_variation.helper

class ProductWithVariation {
    Long productId
    Collection<Long> variationIds = []

    static constraints = {
//        variationIds(nullable: true)
//        productId(nullable: false)
    }

    static hasMany = [
        variationIds: Long
    ]

    static mapping = {
        variationIds fetch: 'join'
    }
}

package com.webcommander.plugin.multi_level_pricing

import com.webcommander.webcommerce.Product

class ProductMultiLevelPrice {
    Long id
    Boolean isActive = true
    Product product

    Long variationId

    Collection<ProductPrice> prices = []

    static hasMany = [prices: ProductPrice]

    static constraints = {
        variationId(nullable: true)
        product(unique: ['variationId'])
    }
}

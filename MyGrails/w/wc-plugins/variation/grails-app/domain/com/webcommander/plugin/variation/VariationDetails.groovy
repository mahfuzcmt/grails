package com.webcommander.plugin.variation

import com.webcommander.webcommerce.Product

class VariationDetails {
    Long id
    String model //standard, enterprise
    Long modelId
    Product product

    static constraints = {
        model maxSize: 10
        modelId(nullable: true)
    }
}
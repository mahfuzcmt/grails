package com.webcommander.plugin.variation.models

import com.webcommander.models.ProductData
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.webcommerce.Product

class VariationProductData extends ProductData {
    Long productVariationId
    String variationModel

    VariationProductData(Product product, ProductVariation variation, String variationModel) {
        super(product)
        productVariationId = variation.id
        this.variationModel = variationModel
    }

    VariationProductData(Product product) {
        super(product)
        isAvailable = false
    }

}

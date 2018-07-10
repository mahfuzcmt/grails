package com.webcommander.plugin.variation.models

import com.webcommander.models.ProductInCart
import grails.util.TypeConvertingMap

class VariationProductInCart extends ProductInCart {

    public VariationProductInCart(VariationProductData data, TypeConvertingMap params) {
        super(data, params)
    }

    public void modifyApiResponse(Map item) {
        item.productVariationId = this.product.productVariationId
    }
}

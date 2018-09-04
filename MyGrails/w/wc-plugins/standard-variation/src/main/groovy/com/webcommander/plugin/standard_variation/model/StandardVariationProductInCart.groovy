package com.webcommander.plugin.standard_variation.model

import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import grails.util.TypeConvertingMap

class StandardVariationProductInCart extends VariationProductInCart {

    StandardVariationProductInCart(VariationProductData data, TypeConvertingMap params) {
        super(data, params)
    }
}

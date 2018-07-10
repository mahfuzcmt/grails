package com.webcommander.plugin.variation.factory

import com.webcommander.item.ProductRawData
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.webcommerce.Product
import grails.util.TypeConvertingMap


interface VariationObjectsFactory {

    VariationProductData getVariationProductData(Product product, ProductVariation variation, String variationModel);

    VariationProductInCart getVariationProductInCart(VariationProductData productData, TypeConvertingMap params);

    ProductRawData getProductRawData(ProductVariation variation);
}
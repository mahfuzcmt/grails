package com.webcommander.plugin.standard_variation.factory

import com.webcommander.item.ProductRawData
import com.webcommander.manager.LicenseManager
import com.webcommander.plugin.standard_variation.model.StandardVariationProductData
import com.webcommander.plugin.standard_variation.model.StandardVariationProductInCart
import com.webcommander.plugin.standard_variation.model.StandardVariationProductRawData
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.factory.VariationObjectsFactory
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.webcommerce.Product
import grails.util.TypeConvertingMap

class StandardVariationObjectsFactory implements VariationObjectsFactory {

    @Override
    VariationProductData getVariationProductData(Product product, ProductVariation variation, String variationModel) {
        if(LicenseManager.isAllowed("allow_standard_variation_feature")) {
            return new StandardVariationProductData(product, variation, variationModel)
        }
        return null
    }

    @Override
    VariationProductInCart getVariationProductInCart(VariationProductData productData, TypeConvertingMap params) {
        return new StandardVariationProductInCart(productData, params)
    }

    @Override
    ProductRawData getProductRawData(ProductVariation variation) {
        if(LicenseManager.isAllowed("allow_standard_variation_feature")) {
            return new StandardVariationProductRawData(variation)
        }
        return null
    }
}

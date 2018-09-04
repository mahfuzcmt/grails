package com.webcommander.plugin.enterprise_variation.factory

import com.webcommander.item.ProductRawData
import com.webcommander.manager.LicenseManager
import com.webcommander.plugin.enterprise_variation.EvariationDetails
import com.webcommander.plugin.enterprise_variation.models.EnterpriseVariationProductData
import com.webcommander.plugin.enterprise_variation.models.EnterpriseVariationProductInCart
import com.webcommander.plugin.enterprise_variation.models.EnterpriseVariationProductRawData
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.factory.VariationObjectsFactory
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.webcommerce.Product
import grails.util.TypeConvertingMap

class EnterpriseVariationObjectsFactory implements VariationObjectsFactory {

    @Override
    VariationProductData getVariationProductData(Product product, ProductVariation variation, String variationModel) {
        if(LicenseManager.isAllowed("allow_enterprise_variation_feature")) {
            return new EnterpriseVariationProductData(product, variation, variationModel)
        }
        return null
    }

    @Override
    VariationProductInCart getVariationProductInCart(VariationProductData productData, TypeConvertingMap params) {
        return new EnterpriseVariationProductInCart(productData, params)
    }

    @Override
    ProductRawData getProductRawData(ProductVariation variation) {
        if(LicenseManager.isAllowed("allow_enterprise_variation_feature")) {
            return new EnterpriseVariationProductRawData(variation)
        }
        return null

    }
}

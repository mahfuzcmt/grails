package com.webcommander.plugin.standard_variation.model

import com.webcommander.item.ProductRawData
import com.webcommander.plugin.standard_variation.SvariationDetails
import com.webcommander.plugin.standard_variation.constant.DomainConstants
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationOption
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductImage

class StandardVariationProductRawData extends ProductRawData {
    String standardVariation
    String baseProduct

    public StandardVariationProductRawData(ProductVariation variation) {
        super(variation.product, ["standardVariation", "baseProduct"])
        Product product = variation.product
        standardVariation = ""
        baseProduct = product.sku

        variation.options.eachWithIndex { VariationOption entry, int i ->
            if(i > 0) {
                standardVariation +=","
            }
            standardVariation+= entry.type.name + ":" + entry.value
        }
        VariationDetails details = variation.details
        SvariationDetails sDetails = SvariationDetails.get(details.modelId)
        if(sDetails) {
            Double basePrice = null
            switch (sDetails.priceAdjustableType) {
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.BASE :
                    basePrice = product.basePrice
                    break
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.FIXED :
                    basePrice = sDetails.price
                    break
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.ADD:
                    basePrice = product.basePrice + sDetails.price
                    break
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.REDUCE :
                    basePrice = product.basePrice <= sDetails.price ? product.basePrice : product.basePrice - sDetails.price
                    break
            }
            this.basePrice = basePrice.toString()
            if(sDetails.imageId) {
                this.image = ProductImage.get(sDetails.imageId)?.name ?: ""
            }
        }
    }
}

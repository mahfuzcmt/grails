package com.webcommander.plugin.standard_variation.model

import com.webcommander.plugin.standard_variation.SvariationDetails
import com.webcommander.plugin.standard_variation.constant.DomainConstants
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationDetails
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductImage
import grails.util.Holders

class StandardVariationProductData extends VariationProductData {
    private static VariationService _variationService

    private static VariationService getVariationService() {
        return _variationService ?: (_variationService = Holders.grailsApplication.mainContext.getBean(VariationService))
    }

    public StandardVariationProductData(Product product, ProductVariation variation, String variationModel) {
        super(product, variation, variationModel)
        VariationDetails details = variation.details
        SvariationDetails sDetails = SvariationDetails.get(details.modelId)
        if(sDetails) {
            switch (sDetails.priceAdjustableType) {
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.FIXED :
                    this.basePrice = sDetails.price
                    break
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.ADD:
                    this.basePrice += sDetails.price
                    break
                case DomainConstants.PRICE_ADJUSTABLE_TYPE.REDUCE :
                    this.basePrice = this.basePrice <= sDetails.price ? this.basePrice : this.basePrice - sDetails.price
                    break
            }
            ProductImage productImage = null
            if(this.images && sDetails.imageId && (productImage = ProductImage.get(sDetails.imageId))) {
                this.addImage([productImage])
            }

            /*TODO: verify "isAvailable" is why*/
            if(!variation.active || !variationService.isAvailable(this)) {
                this.isAvailable = false
            }
        } else {
            this.isAvailable = false
        }
    }
}

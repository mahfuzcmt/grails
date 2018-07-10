package com.webcommander.plugin.enterprise_variation.models

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.enterprise_variation.EvariationDetails
import com.webcommander.plugin.enterprise_variation.constants.EnterpriseProperties
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product

class EnterpriseVariationProductData extends VariationProductData {
    private static VariationService variationService = VariationService.getInstance()

    EnterpriseVariationProductData(Product product, ProductVariation variation, String variationModel) {
        super(product, variation, variationModel)
        def details = variation.details
        def eDetails = EvariationDetails.get(details.modelId)
        if(eDetails) {
            Map prop = eDetails.options.collectEntries {
                def field = it.field
                field = field.substring(field.indexOf(".") + 1)
                if(field == "description") {
                    [(field): it.description.content]
                } else {
                    [(field) : it.value]
                }
            }
            this.name = eDetails.name
            this.sku = eDetails.sku
            this.url = eDetails.url
            if(eDetails.metaTags) {
                this.metaTags = []
                eDetails.metaTags.each {
                    this.metaTags.push([name: it.name, value: it.value])
                }
            }
            EnterpriseProperties.CORE_PROP.each {
                this[it] = eDetails[it]
            }

            EnterpriseProperties.BOOLEAN_PROP.each {
                this[it] = prop[it] == "true"
            }

            EnterpriseProperties.STRING_PROP.each {
                if(prop[it]) {
                    this[it] = prop[it]
                }
            }

            EnterpriseProperties.DOUBLE_PROP.each {
                if(prop[it]) {
                    this[it] = prop[it].toDouble()
                }
            }

            if(prop.minOrderQuantity) {
                this.minOrderQuantity = prop.minOrderQuantity ? prop.minOrderQuantity.toInteger() : 1
                this.maxOrderQuantity = prop.maxOrderQuantity ? prop.maxOrderQuantity.toInteger() : null
            }
            if(prop.multipleOrderQuantity) {
                this.multipleOfOrderQuantity = prop.multipleOrderQuantity.toInteger() ?: 1
            }
            this.supportedMinOrderQuantity =  this.isMultipleOrderQuantity ? this.multipleOfOrderQuantity : (this.minOrderQuantity ?: 1);
            this.supportedMaxOrderQuantity = this.maxOrderQuantity
            boolean considerStock = this.isInventoryEnabled && AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "order_quantity_over_stock") != DomainConstants.OUT_OF_STOCK_MESSAGE_TYPE.SELL_AWAY
            if(considerStock && (!this.supportedMaxOrderQuantity || this.supportedMaxOrderQuantity > eDetails.availableStock)) {
                this.supportedMaxOrderQuantity = eDetails.availableStock
            }
            if(prop.isMultipleOrderQuantity) {
                int counter = 2;
                while(this.supportedMinOrderQuantity < this.minOrderQuantity && (!this.maxOrderQuantity || this.supportedMinOrderQuantity < this.maxOrderQuantity)) {
                    this.supportedMinOrderQuantity = counter++ * this.multipleOfOrderQuantity
                }
                if(this.maxOrderQuantity) {
                    Integer tentativeMax = this.supportedMinOrderQuantity
                    while(tentativeMax <= this.maxOrderQuantity) {
                        this.supportedMaxOrderQuantity = tentativeMax;
                        tentativeMax = counter++ * this.multipleOfOrderQuantity
                    }
                }
            }
            if(eDetails.images) {
                this.addImage(eDetails.images)
            }
            if(eDetails.videos) {
                this.addVideos(eDetails.videos)
            }
            //TODO: verify why isAvailable check
            if(!variation.active || !variationService.isAvailable(this)) {
                this.isAvailable = false
            }
        }
    }
}

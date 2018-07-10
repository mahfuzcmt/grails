package com.webcommander.plugin.multi_level_pricing

import com.webcommander.constants.DomainConstants
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class MultiLevelPricingTagLib {
    static namespace = "multiLevelPricing"
    MultiLevelPricingService multiLevelPricingService
    ProductService productService

    def multiLevelPricingSetting = { Map attrs, body ->
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MULTI_LEVEL_PRICING);
        out << body();
        out << g.render(template: "/plugins/multi_level_pricing/admin/multiLevelPricingSetting", model: [configs: configs])
    }

    def addMultiLevelPricingForEachProduct = { Map attrs, body ->
        out << body();
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MULTI_LEVEL_PRICING);
        Product product = pageScope.product
        ProductMultiLevelPrice multiLevelPrice
        if ((attrs.entityType).equals("variation")) {
            multiLevelPrice = ProductMultiLevelPrice.find {
                eq("variationId", pageScope.variationId as Long)
                eq("product", product)
            }
        } else {
            multiLevelPrice = ProductMultiLevelPrice.find {
                isNull("variationId")
                eq("product", product)
            }
        }

        if(configs.is_enabled == "true") {
            out << g.include(view: "/plugins/multi_level_pricing/admin/_multiLevelPricing.gsp", model: [multiLevelPrice: multiLevelPrice]);
        }
    }

    def priceBlockContainer = { Map attrs, body ->
        def productData = attrs.productData
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MULTI_LEVEL_PRICING)
        ProductMultiLevelPrice multiLevelPrice
        if(productData.hasProperty('productVariationId') && productData.productVariationId){
            multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(Product.get(productData.id),productData.productVariationId)
        } else {
            multiLevelPrice = ProductMultiLevelPrice.findByProduct(Product.get(productData.id))
        }
        if(configs.is_enabled == "true" && multiLevelPrice && multiLevelPrice.isActive && AppUtil.loggedCustomer) {
            Long variationId = productData.hasProperty('productVariationId') ? productData.productVariationId : null
            Double price = multiLevelPricingService.getMultiLevelPriceForProduct(productData.id, variationId)
            if (!price) {
                out << body();
                return
            }
            // TODO need to deal cache for product data
            productData.basePrice = price
            productData.calculatePrice()
            attrs.productData = productData

            out << """<div class="price-block-container" is-on-sale="${productData.isOnSale}" is-expect-to-pay="${productData.isExpectToPay}">
                            <label class="label-for-price">${ site.message(code: "${attrs.config ? (attrs.config.label_for_price ?: "") : (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT).label_for_base_price ?: "")}") }</label>
                            <span class="current-price price">
                                <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="price-amount">${productData.priceToDisplay.toCurrency().toPrice()}</span>""";
                                if(productData.taxMessage){
                                    out << """<span class='tax-message'>${productData.taxMessage.encodeAsBMHTML()}</span>""";
                                }
                            out << """</span>
                        </div>""";

        } else {
            out << body();
        }
    }

    def incompleteDataCartPopup = { Map attrs, body ->
        def productData = attrs.productData
        Integer quantity = attrs.quantity
        Long variationId = productData.hasProperty('productVariationId') ? productData.productVariationId : null
        Double price = multiLevelPricingService.getMultiLevelPriceForProduct(productData.id, variationId)
        ProductMultiLevelPrice multiLevelPrice
        if(productData.hasProperty('productVariationId') && productData.productVariationId){
            multiLevelPrice = ProductMultiLevelPrice.findByProductAndVariationId(Product.get(productData.id),productData.productVariationId)
        } else {
            multiLevelPrice = ProductMultiLevelPrice.findByProduct(Product.get(productData.id))
        }
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MULTI_LEVEL_PRICING);
        if(configs.is_enabled == "true" && multiLevelPrice &&  multiLevelPrice.isActive && AppUtil.loggedCustomer && price) {
            out << """<span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="price-amount">${price.toCurrency().toPrice()}</span>
                                X
                                <span class="quantity">${quantity}</span>
                                =
                                <span class="currency-symbol">${AppUtil.siteCurrency.symbol}</span><span class="total-amount">${(price * quantity).toCurrency().toPrice()}</span>""";
        } else {
            out << body();
        }
    }

}

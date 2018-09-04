package com.webcommander.plugin.variation.factory

import com.webcommander.constants.NamedConstants
import com.webcommander.item.ProductRawData
import com.webcommander.manager.CacheManager
import com.webcommander.models.ProductData
import com.webcommander.plugin.variation.OrderVariationItem
import com.webcommander.plugin.variation.ProductVariation
import com.webcommander.plugin.variation.VariationService
import com.webcommander.plugin.variation.models.VariationServiceModel
import com.webcommander.plugin.variation.models.VariationProductData
import com.webcommander.plugin.variation.models.VariationProductInCart
import com.webcommander.webcommerce.Product
import grails.util.Holders
import grails.util.TypeConvertingMap

class VariationObjectsProducer {
    private static Map<String, VariationObjectsFactory> factories =  new HashMap<String, VariationObjectsFactory>()
    private static Map<String, VariationServiceModel> variationServiceHolder =  new HashMap<String, VariationObjectsFactory>()

    private static VariationService _variationService

    private static VariationService getVariationService() {
        return _variationService ?: (_variationService = Holders.grailsApplication.mainContext.getBean(VariationService))
    }

    static VariationObjectsFactory getVariationObjectsFactory(String type) {
        return factories.get(type)
    }

    static void setVariationObjectsFactory(String type, VariationObjectsFactory factory) {
        factories.put(type, factory)
    }

    static void setVariationServiceBean(String type,  VariationServiceModel variationService) {
        variationServiceHolder.put(type, variationService)
    }

    static VariationServiceModel getVariationServiceBean(String type) {
        return variationServiceHolder.get(type)
    }

    /**
     * config [:] for first active variation
     * config null for no variation
     * [options: [1, 2, 3]] or [variation: 5]
    **/

    static VariationProductData getVariationProductData(Product product, Map config) {
        if(config == null) {
            return null
        }
        ProductVariation variation = null
        Boolean requiredVariationData = false
        if(config.variation) {
            variation = ProductVariation.get(config.variation)
            requiredVariationData = true
        } else if(config.orderItemId) {
            OrderVariationItem variationItem = OrderVariationItem.createCriteria().get {
                eq("orderItem.id", config.orderItemId.toLong())
            }
            if(variationItem) {
                variation = ProductVariation.get(variationItem.variationId)
                requiredVariationData = true
            } else {
                requiredVariationData = variationService.hasVariation(product.id)
            }
        } else if(config.variations) {
            variation = variationService.getVariationByOptionList(product, config.variations)
            requiredVariationData = true
        } else if(config.options) {
            List options = config.options instanceof String ? [config.options] : config.options as List
            variation = variationService.getVariationByOptions(product, options)
            requiredVariationData = true
        } else {
            variation = variationService.getVariationByOptions(product, [])
        }
        if(variation == null ) {
            return requiredVariationData ? new VariationProductData(product) : null
        }

        VariationProductData productData = null

        // TODO as we are planing to remove cache machanism for product data
        /*VariationProductData productData = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "product", "product-" + product.id, "product-variation-" + variation.id)
        if(productData) { return  productData }*/

        String type = variationService.getVariationModel(product.id)
        VariationObjectsFactory dataFactory = getVariationObjectsFactory(type)
        productData = dataFactory.getVariationProductData(product, variation, type)
        productData.attrs.put("selectedVariation", variation.id)

        /*if(productData) {
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, productData, "product", "product-" + product.id, "product-variation-" + variation.id)
        }*/

        return productData
    }

    static VariationProductInCart getVariationProductInCart(ProductData productData, TypeConvertingMap params) {
        VariationObjectsFactory dataFactory
        if(productData instanceof VariationProductData && (dataFactory= getVariationObjectsFactory(productData.variationModel))) {
            return dataFactory.getVariationProductInCart(productData, params)
        }
        return null
    }

    static ProductRawData getProductRawData(ProductVariation variation) {
        String type = variation.details.model
        VariationObjectsFactory dataFactory = getVariationObjectsFactory(type)
        return dataFactory.getProductRawData(variation)
    }
}

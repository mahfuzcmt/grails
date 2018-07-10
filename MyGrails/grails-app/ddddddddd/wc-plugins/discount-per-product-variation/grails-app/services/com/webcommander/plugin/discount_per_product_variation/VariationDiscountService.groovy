package com.webcommander.plugin.discount_per_product_variation

import com.webcommander.plugin.discount_per_product_variation.controllers.VariationDiscountAdminController
import com.webcommander.plugin.discount_per_product_variation.helper.ProductWithVariation
import com.webcommander.plugin.variation.ProductVariation
import grails.gorm.transactions.Transactional

@Transactional
class VariationDiscountService {

    def getVariations(Long productId) {
        return ProductVariation.where {
            eq("product.id", productId)
        }
    }

    def getVariationIds(Long productId) {
        return ProductVariation.createCriteria().list {
            projections {
                property('id')
            }
            eq("product.id", productId)
        }
    }

    def saveVariationInfo(Map response, Map params) {
        def discountId = (params.discountId).toLong()
        VariationDiscountDetails variationDiscountDetails = VariationDiscountDetails.findByDiscountId(discountId) ?: new VariationDiscountDetails()
        if(!variationDiscountDetails.discountId)    variationDiscountDetails.discountId = discountId
        else {
            variationDiscountDetails.productWithVariations.each {
                it.delete()
            }
            variationDiscountDetails.productWithVariations = []
        }
        VariationDiscountAdminController.selectedVariations?.each { key, value ->
            ProductWithVariation productWithVariation = new ProductWithVariation()
            productWithVariation.productId = key.toLong()
            value?.each {
                productWithVariation.variationIds.add(it as Long)
            }
            variationDiscountDetails.productWithVariations.add(productWithVariation)
        }
        variationDiscountDetails.save()
    }

    def getSelectedVariationInfo(Map response, Map params) {
        def discountId = (params.discountId).toLong()
        VariationDiscountDetails variationDiscountDetails = VariationDiscountDetails.findByDiscountId(discountId) ?: null
        if(variationDiscountDetails) {
            response.variationInfo = variationDiscountDetails?.productWithVariations
            response.success = true
        }
        return response
    }

    def getVariationDiscountDeatils(Long[] ids) {
        if(ids) return VariationDiscountDetails.getAll(ids)
        else    return VariationDiscountDetails.getAll()
    }
}

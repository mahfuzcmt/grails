package com.webcommander.plugin.discount_per_product_variation.controllers

import com.webcommander.plugin.discount_per_product_variation.VariationDiscountService
import grails.converters.JSON

class VariationDiscountAdminController {

    def static selectedVariations;
    VariationDiscountService variationDiscountService

    def showProductVariation() {
        def variations = variationDiscountService.getVariations(params.productId as Long)
        render(view: "/plugins/discount_per_product_variation/admin/listProductVariation", model: [variations: variations, selectedVariations: params.list('selectedVariations')])
    }

    def getProductVariationList() {
        def variations = variationDiscountService.getVariationIds(params.productId as Long)
        render([status: "success", variations: "$variations"] as JSON)
    }

    def cacheVariationData() {
        selectedVariations = JSON.parse(params.selectedVariation)
        render([status: "success"] as JSON)
    }

    def getAllSelectedVariations() {
        Map selectedVariation = [:]
        variationDiscountService.getVariationDiscountDeatils(null).each {
            it.productWithVariations.each {
                if(!selectedVariation."$it.productId")   selectedVariation."$it.productId" = []
                selectedVariation."$it.productId" = it.variationIds
            }
        }
        render([selectedVariations: selectedVariation] as JSON)
    }
}

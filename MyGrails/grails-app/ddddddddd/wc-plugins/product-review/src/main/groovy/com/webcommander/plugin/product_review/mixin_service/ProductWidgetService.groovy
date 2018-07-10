package com.webcommander.plugin.product_review.mixin_service

import com.webcommander.plugin.product_review.ProductReviewService
import grails.util.Holders
import grails.util.TypeConvertingMap

/**
 * Created by arman on 2/9/2016.
 */
class ProductWidgetService {
    ProductReviewService productReviewService = Holders.grailsApplication.mainContext.getBean(ProductReviewService)
    def renderProductReviewWidget(Map attrs, Writer writer) {
        TypeConvertingMap params = [:]
        params.productId = attrs.product.id
        params.forRender = 'true'
        Double avgRating = productReviewService.getAvgRatingByProduct(params);
        renderService.renderView("/plugins/product_review/productWidget/productReview", [avgRating: avgRating], writer)
    }

    def renderProductReviewWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/product_review/productWidget/editor/productReview", [:], writer)
    }
}

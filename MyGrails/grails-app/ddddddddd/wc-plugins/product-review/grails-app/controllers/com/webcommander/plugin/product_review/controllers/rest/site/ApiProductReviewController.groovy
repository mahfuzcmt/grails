package com.webcommander.plugin.product_review.controllers.rest.site

import com.webcommander.plugin.product_review.ProductReview
import com.webcommander.plugin.product_review.ProductReviewService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import grails.util.TypeConvertingMap

class ApiProductReviewController extends RestProcessor{
    ProductReviewService productReviewService

    def list() {
        Long productId = params.long("productId")
        if(productId == null) {
            throw new ApiException("product.id.is.required")
        }
        Map filterMap = new TypeConvertingMap()
        filterMap.productId = productId
        filterMap.max = params.max ?: -1
        filterMap.offset = params.offset ?: 0
        filterMap.forRender = true
        List<ProductReview>  reviews = productReviewService.getReviewList(filterMap)
        rest(reviews: reviews)
    }

    def avgRating() {
        Long productId = params.long("productId")
        if(productId == null) {
            throw new ApiException("product.id.is.required")
        }
        Map filterMap = new TypeConvertingMap()
        filterMap.productId = productId
        filterMap.forRender = true
        rest([
            avgRating: productReviewService.getAvgRatingByProduct(filterMap),
            count: productReviewService.getProductReviewCount(filterMap),
            reviewCount: productReviewService.getProductReviewCommentCount(filterMap)
        ])
    }

    def add() {
        Map data = [name: params.name, email: params.email, score: params.rating, customerId: AppUtil.loggedCustomer, productId: params.productId, review: params.review]
        Boolean result = productReviewService.saveReview(data)
        if(result) {
            rest(status: "success")
        } else {
            throw new ApiException("review.could.not.save")
        }
    }
}

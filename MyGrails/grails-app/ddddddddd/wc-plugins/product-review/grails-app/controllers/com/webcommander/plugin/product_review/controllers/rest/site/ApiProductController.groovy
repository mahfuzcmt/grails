package com.webcommander.plugin.product_review.controllers.rest.site

import com.webcommander.plugin.product_review.ProductReview
import com.webcommander.plugin.product_review.ProductReviewService
import com.webcommander.util.RestProcessor
import grails.util.Holders
import grails.web.Action

/**
 * Created by sajedur on 3/12/2015.
 */
class ApiProductController extends RestProcessor{

    ProductReviewService productReviewService

    def reviews() {
        params.productId = params.id
        List<ProductReview> reviews = params.productId ? productReviewService.getReviewList(params) : [];
        rest reviews: reviews
    }

}

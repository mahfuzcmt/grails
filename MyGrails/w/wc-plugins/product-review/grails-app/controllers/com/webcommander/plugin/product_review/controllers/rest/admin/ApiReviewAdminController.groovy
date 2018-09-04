package com.webcommander.plugin.product_review.controllers.rest.admin

import com.webcommander.plugin.product_review.ProductReview
import com.webcommander.plugin.product_review.ProductReviewService
import com.webcommander.util.RestProcessor

class ApiReviewAdminController extends RestProcessor{
    ProductReviewService productReviewService
    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<ProductReview> reviews = productReviewService.getReviewList(params)
        rest reviews: reviews
    }

    def changeStatus() {
        productReviewService.changeStatus(params.long("id"), params.status)
        rest([status: "success"])
    }
}

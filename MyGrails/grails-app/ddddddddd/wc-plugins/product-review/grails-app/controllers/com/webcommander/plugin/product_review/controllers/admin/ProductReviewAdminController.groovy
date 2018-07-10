package com.webcommander.plugin.product_review.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.product_review.ProductReview
import com.webcommander.plugin.product_review.ProductReviewService
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.authentication.annotations.Restriction

class ProductReviewAdminController {
    ProductReviewService productReviewService
    CommonService commonService

    @License(required = "allow_review_rating_feature")
    @Restriction(permission = "product_review.view.list")
    def loadAppView(){
        params.max = params.max ?: "10";
        Integer count = productReviewService.getProductReviewCount(params);
        List<ProductReview> reviewList = commonService.withOffset(params.max, params.offset, count) {max , offset, _count ->
            params.max = max;
            params.offset = offset;
            productReviewService.getReviewList(params)
        }
        render(view: "/plugins/product_review/admin/appView", model: [reviews: reviewList, count: count]);
    }

    def edit() {
        ProductReview review = productReviewService.getReview(params.long("id"))
        render(view: "/plugins/product_review/admin/infoEdit", model: [review: review])
    }
    def view() {
        ProductReview review = productReviewService.getReview(params.long("id"))
        render(view: "/plugins/product_review/admin/infoView", model: [review: review])
    }
    def save(){
        def result = productReviewService.saveReview(params);
        if (result) {
            render([status: "success", message: g.message(code: "product.review.update.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "product.review.could.not.updated")] as JSON)
        }
    }
    @License(required = "allow_review_rating_feature")
    @Restriction(permission = "product_review.change.status")
    def changeStatus() {
        def result = productReviewService.changeStatus(params.long("id"), params.status);
        if(result){
            render([status: "success", message: g.message(code: "product.review.status.change.success")] as JSON )
        } else {
            render([status: "error", message: g.message(code: "product.review.status.could.not.changed")] as JSON )
        }
    }
    @License(required = "allow_review_rating_feature")
    @Restriction(permission = "product_review.remove", entity_param = "id", domain = ProductReview)
    def delete() {
        if (productReviewService.deleteReview(params)) {
            render([status: "success", message: g.message(code: "product.review.delete.success")] as JSON )
        } else {
            render([status: "error", message: g.message(code: "product.review.could.not.deleted")] as JSON )
        }
    }
    def advanceFilter(){
        render(view: "/plugins/product_review/admin/filter")
    }
    def config(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING)
        render(view: "/plugins/product_review/admin/config", model: [config: config])
    }
}

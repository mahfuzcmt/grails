package com.webcommander.plugin.product_review.controllers.site

import com.webcommander.captcha.CaptchaService
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.product_review.ProductReview
import com.webcommander.plugin.product_review.ProductReviewService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class ProductReviewController {
    ProductReviewService productReviewService
    CommonService commonService
    CaptchaService captchaService

    def loadReview() {
        params.forRender = true;
        params.max = params.max ?: AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING, "review_per_page");
        params.offset = params.offset ?: "0";
        Integer totalReview = productReviewService.getProductReviewCommentCount(params)
        List<ProductReview> reviews =commonService.withOffset(params.max, params.offset, totalReview) { max, offset, _count ->
            params.offset = offset
            productReviewService.getReviewList(params);
        }
        Integer count = productReviewService.getProductReviewCount(params)
        Double avgRating = productReviewService.getAvgRatingByProduct(params);
        render(view: "/plugins/product_review/site/loadReview", model: [reviews: reviews, count: count, totalComment: totalReview, avgRating: avgRating, params: params]);
    }

    def review() {
        render(view: "/plugins/product_review/site/productReview")
    }

    def saveReview() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING);
        if (!config.product_review == "on" || (!(config.who_can_review == "every_one") && !session.customer)) {
            render([status: "error", message: g.message(code: "login.to.review"), login: true] as JSON)
            return;
        }
        if (!params.name || !params.email) {
            if (session.customer) {
                params.customerId = session.customer.toLong();
            } else {
                render([status: "error", message: g.message(code: "product.review.could.not.submitted")] as JSON)
                return;
            }
        }
        def captchaType = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_type");
        def captchaConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "captcha_setting")
        if(captchaConfig == "enable" && !captchaService.validateCaptcha(params, request)) {
            render([status: "error", captchaValidation: "failure", message: g.message(code: "invalid.captcha.entry"), captchaType: captchaType] as JSON)
            return;
        }
        def result = productReviewService.saveReview(params);
        if (result && config.show_review == "immediately"){
            String message;
            if (params.double("score") > 0.0) {
                message = "thank.for.rating";
            } else {
                message = "thank.for.review";
            }
            render([status: "success", action: "reload", message: g.message(code: message), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON);
        } else if(result) {
            render([status: "success", message: g.message(code: "product.review.save.success.message"), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON);
        } else {
            render([status: "error", message: g.message(code: "product.review.could.not.submitted"), captchaType: captchaConfig == "enable" ? captchaType : "disabled"] as JSON)
        }
    }
}

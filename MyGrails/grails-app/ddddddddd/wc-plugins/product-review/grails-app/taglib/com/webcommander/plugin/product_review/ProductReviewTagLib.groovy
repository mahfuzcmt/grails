package com.webcommander.plugin.product_review

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.models.ProductData
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import grails.web.servlet.mvc.GrailsParameterMap
import grails.util.TypeConvertingMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class ProductReviewTagLib {
    static namespace = "review"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app
    ProductReviewService productReviewService

    static String raty_js = "plugins/product-review/js/jquery/jquery.raty.min.js"
    static String raty_inits_js = "plugins/product-review/js/site-js/raty-inits.js"
    static String product_review_js = "plugins/product-review/js/site-js/product-review.js"

    def tabHeader = { attrs, body ->
        out << body()
        Boolean review = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING, "product_review").toBoolean()
        if(review && LicenseManager.isAllowed("allow_review_rating_feature")) {
            app.enqueueSiteJs(src: raty_js, scriptId: "raty")
            app.enqueueSiteJs(src: product_review_js, scriptId: "product-review")
            out << '<div class="bmui-tab-header" data-tabify-tab-id="reviewAndRating" data-tabify-url="' + app.relativeBaseUrl()
            out << 'productReview/review?productId=' + attrs.productId + '"'
            out << 'load_url="' + app.relativeBaseUrl() + 'productReview/review?productId=' + attrs.productId + '">'
            out << '<span class="title">' + g.message(code: "review.rating") + '</span>'
            out << '</div>'
        }
    }

    def tabBody = { attrs, body ->
        out << body()
        Boolean review = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REVIEW_RATING, "product_review").toBoolean()
        if(review) {
            out << '<div id="bmui-tab-reviewAndRating">'
            out << '</div>'
        }
    }

    def adminJSs = { attrs, body ->
        out << body()
        out << app.javascript(src: raty_js)
        out << app.javascript(src: 'plugins/product-review/js/shared/review-rating-widget-share.js')
    }

    def reviewAtCompareDetails = { attrs, body ->
        out << body();
        Boolean config = request.config["is_rating_active"].toBoolean();
        if (!config) {
            return;
        }
        app.enqueueSiteJs(src: raty_js, scriptId: "raty")
        app.enqueueSiteJs(src: raty_inits_js, scriptId: "raty-init")
        List<ProductData> datas = pageScope.productList
        List<Double> ratings = datas.collect {
            GrailsParameterMap param = new GrailsParameterMap([forRender: true, productId: it.id], request);
            return productReviewService.getAvgRatingByProduct(param);
        }
        out << '<tr class="rating-row">';
        out << "<th>${g.message(code: "rating")}</th>"
        ratings.each {
            out << "<td>" + renderRatingField(isReadOnly: true, score: it) + "</td>"
        }
        out << '</tr>';
    }

    def ratingConfig = { attr, body ->
        String configKey = "is_rating_active"
        out << body()
        out << '<div class="form-row rating">'
        out << '<label>&nbsp;</label> '
        String checked = attr.configs?."$configKey" == "true" ? "checked='checked'" : ""
        if(attr.configType) {
            out << "<input type='checkbox' class='single' name='${attr.configType}.${configKey}' value='true'" + "uncheck-value='false' ${checked}>"
        } else {
            out << "<input type='checkbox' class='single' name='${configKey}' value='true' uncheck-value='false' ${checked}/>"
        }
        out << "<span>${g.message(code: "rating")}</span>"
        out << '</div>'
    }

    def productWidgetConfig = { attrs, body ->
        String configKey = "is_rating_active"
        out << body()
        out << '<div class="sidebar-group">'
        out << '<div class="sidebar-group-body">'
        out << "<input type='checkbox' class='single' name='${configKey}' value='true' uncheck-value='false' ${pageScope.config[configKey] == 'true' ? 'checked' : ''}>"
        out << "<label>${g.message(code: "rating")}</label>"
        out << '</div>'
        out << '</div>'
    }

    def renderRatingField = { attrs, body ->
        out << body()
        app.enqueueSiteJs(src: raty_js, scriptId: "raty")
        app.enqueueSiteJs(src: raty_inits_js, scriptId: "raty-init")
        out << "<div class='review-rating ${attrs.isReadOnly ? "read-only" : ""}' ${attrs.score ? "score='${attrs.score}'" : ""}></div>"
        if (!attrs.isReadOnly && attrs.target) {
            out << "<input type='${attrs.target.type ?: "hidden"}' name='${attrs.target.name ?: "score"}'>"
        }
    }

    def renderRatingInImageView = { attrs, body ->
        out << body()
        if (attrs.config.is_rating_active == "true") {
            Double score = productReviewService.getAvgRatingByProduct(new TypeConvertingMap([forRender: true, productId: attrs.product.id]))
            out << renderRatingField(isReadOnly: true, score: score)
        }
    }

    def renderRatingInListView = { attrs, body ->
        if (attrs.config.is_rating_active == "true") {
            Double score = productReviewService.getAvgRatingByProduct(new TypeConvertingMap([forRender: true, productId: attrs.product.id]))
            out << renderRatingField(isReadOnly: true, score: score)
        }
        out << body()
    }

    def appendSortTypes = { attrs, body ->
        out << body()
        Map config = attrs.config
        if (config.is_rating_active == "true") {
            out << "<option value='RATING_DESC' " + (config["product-sorting"] == 'RATING_DESC' ? 'selected' : '') + ">" + g.message(code: "rating.high.to.low") + "</option>"
            out << "<option value='RATING_ASC' " + (config["product-sorting"] == 'RATING_ASC' ? 'selected' : '') + ">" + g.message(code: "rating.low.to.high") + "</option>"
        }
    }

    def showRatingInWidget = { attrs, body ->
        out << body()
        out << "<div class='rating-select'>"
        out << "<input type= 'checkbox' class= 'single' name='filterConfig' value='rating'" + "${attrs.config.filterConfig.contains('rating') ? 'checked=checked' : ''}" + ">"
        out << "<label>" + g.message(code: "rating") + "</label>"
        out << "</div>"

    }

    def configProductInCategoryPage = { attrs, body ->
        out << body()
        out << """<div class="form-row">
                    <input type="checkbox" class="single" name="category_page.is_rating_active" value="true" uncheck-value="false" ${
            pageScope.config["is_rating_active"] == "true" ? "checked" : ""
        }>
                    <span>${g.message(code: "rating")}</span>
                </div>"""
    }
}
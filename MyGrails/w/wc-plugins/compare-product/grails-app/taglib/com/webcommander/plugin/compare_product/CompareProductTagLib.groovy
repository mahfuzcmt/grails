package com.webcommander.plugin.compare_product

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class CompareProductTagLib {
    static namespace = "productCompare"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def adminJSs = { attrs, body ->
        out << body()
        def url = app.relativeBaseUrl() + "plugins/compare-product/js";
        out << app.javascript(src: "$url/admin/product-custom-properties.js")
        out << app.javascript(src: "$url/admin/jquery.autocomplete.js")
    }

    def customerProfileTabHeader = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CUSTOMER_PROFILE_PAGE, "add_to_compare")
        if(config == "true") {
            app.enqueueSiteJs(src: "plugins/compare-product/js/customer-profile.js", scriptId: "compare-product-customer-profile")
        }
    }

    def addToCompareForEditor = { attr, body ->
        out << body()
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT, "add_to_compare")?.toBoolean()) {
            out << "<span class='add-to-compare-button button'  title='${g.message(code: "add.to.compare")}'>"+ g.message(code: "add.to.compare") + "</span>"
        }

    }

    def addToCompare = { attr, body ->
        out << body()
        if(!pageScope.config.add_to_compare?.toBoolean()) {
            return
        }
        app.enqueueSiteJs(src: "plugins/compare-product/js/compare-product.js", scriptId: "compare-product")
        Long productId = attr.productId ?: attr.product?.id;
        if(session.compare && session.compare.find { it == productId }) {
            out << "<span class='remove-from-compare-button button' product-id='${productId}' title='${g.message(code: "remove.from.compare")}'>"+ g.message(code: "remove.from.compare") + "</span>"
        } else {
            out << "<span class='add-to-compare-button button' product-id='${productId}' title='${g.message(code: "add.to.compare")}'>"+ g.message(code: "add.to.compare") + "</span>"
        }
    }

    def productWidgetConfig = { attr, body ->
        String check = pageScope.config.add_to_compare?.toBoolean() ? " checked" : ""
        out << body()
        out << '<div class="sidebar-group">'
        out << '<div class="sidebar-group-body">'
        out << '<input type="checkbox" class="single" name="add_to_compare" value="true" uncheck-value="false"' + check + '>'
        out << '<label>' + g.message(code: "add.to.compare") + '</label>'
        out << '</div>'
        out << '</div>'
    }

    def productPageConfig = { attr, body ->
        String check = pageScope.productSettings.add_to_compare?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="product.add_to_compare" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.compare") + '</span>'
        out << '</div>'
        out << body()
    }

    def categoryPageConfig = { attr, body ->
        String check = pageScope.config.add_to_compare?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="category_page.add_to_compare" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.compare") + '</span>'
        out << '</div>'
        out << body()
    }

    def configSearchPage = { attr, body ->
        String check = pageScope.searchConfig.add_to_compare?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="search_page.add_to_compare" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.compare") + '</span>'
        out << '</div>'
        out << body()
    }

    def addToProductEditor = { attr, body ->
        out << body()
        Product product = Product.get(pageScope.productId)
        if(product.productType != DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << '<div class="bmui-tab-header" data-tabify-tab-id="customProperties" data-tabify-url="' + app.relativeBaseUrl() + 'compareProductAdmin/loadCustomProperties?productId=' + product.id +'">'
            out << '<span class="title">' + g.message(code: "custom.properties") + '</span>'
            out << '</div>'
        }
    }
}
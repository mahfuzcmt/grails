package com.webcommander.plugin.filter

import com.webcommander.constants.DomainConstants
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class FilterTagLib {
    static namespace = "filter"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    static String raty_inits_js = "plugins/filter/js/site-js/raty-inits.js"
    static String raty_js = "plugins/filter/js/jquery/jquery.raty.min.js"

    def addFilterProfileInCategory = { attr, body ->
        out << body()
        out << '<div class="form-row chosen-wrapper">'
        out << '<span>' + g.message(code: "filter.profile") + '</span>'
        Category category = pageScope.variables.get("category")
        def filterProfile = FilterProfile.createCriteria().get {
            categories {
                eq("id", category.id)
            }
        }
        out << ui.domainSelect( name: "filterProfile", class: "form-full-width filter-profile-select", domain: FilterProfile,
                'custom-attrs' : ['data-placeholder': g.message(code: "select.filter.profile")],
                value: filterProfile ? filterProfile.id : (category?.id ? "none" : "default"),
                prepend: ["default": g.message(code: 'default'), "none": g.message(code: "none")])
        out << '</div>'
    }

    def filterJss = { attrs, body ->
        out << body()
        out << app.javascript(src: '" + raty_js + "')
        out << app.javascript(src: '" + raty_inits_js + "')
    }

    def addToProductEditor = { attr, body ->
        out << body()
        Product product = Product.get(pageScope.productId)
        if(product.productType != DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << '<div class="bmui-tab-header" data-tabify-tab-id="additionalProperties" data-tabify-url="' + app.relativeBaseUrl() + 'filterGroup/loadAdditionalProperties?productId=' + product.id +'">'
            out << '<span class="title">' + g.message(code: "additional.properties") + '</span>'
            out << '</div>'
        }
    }

}



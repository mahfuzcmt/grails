package com.webcommander.plugin.compare_product.mixin_service

import grails.util.Holders
import com.webcommander.ApplicationTagLib

/**
 * Created by sajed on 5/22/2014.
 */
class ProductWidgetService {

    static ApplicationTagLib _app
    static getApp() {
        if(!_app) {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
        return _app
    }

    def renderCompareProductWidget(Map attrs, Writer writer) {
        app.enqueueSiteJs(src: "plugins/compare-product/js/compare-product.js", scriptId: "compare-product")
        renderService.renderView("/plugins/compare_product/productWidget/productCompare", [:], writer)
    }

    def renderCompareProductWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/compare_product/productWidget/editor/productCompare", [:], writer)
    }
}

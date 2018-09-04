package com.webcommander.plugin.product_quick_view


class QuickViewTagLib {
    static namespace = "quickViewSpace"

    def siteJs = { attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/product-quick-view/js/site/quick-view.js')
    }
}
package com.webcommander.plugin.auto_product_load_on_scroll.app

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class ProductAutoScrollTagLib {
    static namespace = "productAutoScroll"

    def siteJs = { attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/auto-product-load-on-scroll/js/site/product-scroller.js')
    }

    def loadCategorySettings = { attrs, body ->
        out << body()
        String categoryPage = DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE
        def config = AppUtil.getConfig(categoryPage)
        out << g.include(view: "plugins/auto_product_load_on_scroll/admin/commonConfig.gsp", model: [config: config, configType: categoryPage, toggleTarget: "pagination"])
    }

    def loadWidgetSettings = { attrs, body ->
        out << body()
        out << g.include(view: "plugins/auto_product_load_on_scroll/admin/commonConfig.gsp", model: [config: params.config, toggleTarget: "item-per-page"])
    }

}
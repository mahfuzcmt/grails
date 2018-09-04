package com.webcommander.plugin.auto_product_load_on_scroll.controllers.site

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.auto_product_load_on_scroll.ProductScrollService
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap

class ProductScrollController {

    ProductService productService
    ProductScrollService productScrollService

    def loadProductScrollView() {
        def productConfig = productScrollService.getConfigForProduct()
        def config = productConfig.config
        int initial_item = config.initial_item.toInteger()
        int increment = config.item_on_scroll.toInteger()
        def productIds = productConfig.productIds
        String views = config["display-type"] == "image" ? "site/productImageView.gsp" : "site/productListView.gsp"
        GrailsParameterMap params = AppUtil.params;
        Map filterMap = [:];
        filterMap["product-sorting"] = params["sort"]
        int max = -1;
        int offset = 0;
        if (config["show-pagination"] == "none" && config['display-type'] != NamedConstants.PRODUCT_WIDGET_VIEW.SCROLLABLE) {
            offset = params.int("offset") ? params.int("offset") + increment : initial_item;
            max = params.int("max") ?: increment
        }
        Integer totalCount = productService.filterOutAvailableProductCount(productIds, filterMap)
        boolean filtered = false
        def productList = productService.getProductData(productIds, filterMap, filtered)
        Integer end
        try {
            if(!filterMap["product-sorting"] && params.type != "category") {
                productList = SortAndSearchUtil.sortInCustomOrder(productList, "id", productIds)
            }
            end = (offset + max) > productList.size() ? productList.size() : (offset + max)
            productList = productList.subList(offset, end)
            if (!productList) {
                throw Exception
            }
        } catch (Exception e) {
            render([status: "error"] as JSON)
            return
        }
        if (end == totalCount) {
            end = 0
        }
        String html = g.include(view: views, model: [productList: productList, config: config, max: max, offset: offset, totalCount: totalCount])
        render([status: "success", html: html, offset: offset, displayType: config["display-type"], end: end] as JSON)
    }
}

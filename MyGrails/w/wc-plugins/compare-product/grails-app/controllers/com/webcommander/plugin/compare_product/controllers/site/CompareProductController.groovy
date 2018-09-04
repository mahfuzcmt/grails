package com.webcommander.plugin.compare_product.controllers.site

import com.webcommander.constants.DomainConstants
import com.webcommander.design.WidgetService
import com.webcommander.models.ProductData
import com.webcommander.plugin.compare_product.CompareProductService
import com.webcommander.plugin.compare_product.manager.CompareManager
import com.webcommander.plugin.compare_product.models.CustomCompareData
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.webcommerce.ProductService
import com.webcommander.widget.Widget
import grails.converters.JSON

class CompareProductController {
    WidgetService widgetService
    ProductService productService
    CompareProductService compareProductService

    def addToCompare() {
        Boolean success
        String error
        Long id = params.long("productId");
        ProductData productData;
        try {
            productData = CompareManager.addToCompare(id)
            success = true;

        } catch (Exception e) {
            success = false
            error = e.message;
        }
        def html = g.include(view:  "/plugins/compare_product/addToComparePopup.gsp", model: [data: productData , success: success, error: error])
        render([status: success ? "success" : "error", html: html.toString()] as JSON);
    }

    def details () {
        if (!session.compare || session.compare.size() < 1) {
            redirect(uri: "/")
            return;
        }
        List productIds = session.compare ?: [];
        List filteredIds = productService.filterAvailableProducts(productIds, [:])
        List<ProductData> productList = SortAndSearchUtil.sortInCustomOrder(productService.getProductData(filteredIds, [:]), "id", productIds)
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.COMPARE_PRODUCT);
        List<CustomCompareData> customCompareDatas = compareProductService.sortCustomProperties(productList.id)
        render(view: "/site/siteAutoPage", model: [
                name: DomainConstants.AUTO_GENERATED_PAGES.COMPARE_PRODUCT_DETAILS,
                view: "/plugins/compare_product/details.gsp",
                config: config,
                productList: productList,
                customCompareDatas: customCompareDatas
        ])
    }

    def widget() {
        Widget widget = Widget.get(params.id);
        render(text: "")
        widgetService.renderCompareProductWidget(widget, response.writer)
    }

    def removeFromCompare() {
        Long productId = params.long("productId")
        CompareManager.removeFromCompare(productId)
        render([status:  "success"] as JSON)
    }

    def removeCompare() {
        CompareManager.removeCompare();
        render([status: "success"] as JSON);
    }
}

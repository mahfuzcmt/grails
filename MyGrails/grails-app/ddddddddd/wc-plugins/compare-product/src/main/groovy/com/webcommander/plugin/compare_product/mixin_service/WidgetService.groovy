package com.webcommander.plugin.compare_product.mixin_service

import com.webcommander.models.ProductData
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.widget.Widget
import com.webcommander.util.AppUtil

class WidgetService {

    def populateCompareProductInitialContentNConfig(Widget widget) {
    }

    def renderCompareProductWidget(Widget widget, Writer writer) {
        def session = AppUtil.session
        List<Long> ids = session.compare ?: []
        List<ProductData> dataList = SortAndSearchUtil.sortInCustomOrder(productService.getProductData(ids, [:]), "id", ids)
        renderService.renderView("/plugins/compare_product/widget/compareProduct", [widget: widget, config: [:], dataList: dataList], writer)
    }
}

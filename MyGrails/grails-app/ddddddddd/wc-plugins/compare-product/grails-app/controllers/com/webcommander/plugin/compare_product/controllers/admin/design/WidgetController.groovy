package com.webcommander.plugin.compare_product.controllers.admin.design



/**
 * Created by sajed on 5/22/2014.
 */
class WidgetController {

    def widgetService

    def saveCompareProductWidget() {
        render(widgetService.saveAnyWidget("CompareProduct", params))
    }
}

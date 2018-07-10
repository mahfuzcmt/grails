package com.webcommander.plugin.auto_product_load_on_scroll

import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.converters.JSON
import com.webcommander.webcommerce.Category
import grails.web.servlet.mvc.GrailsParameterMap

class ProductScrollService {
    def getConfigForProduct() {
        GrailsParameterMap params = AppUtil.params
        def config = [:]
        def productIds = []
        if (params.id && params.type == "productWidget") {
            Widget widget = Widget.get(params.id.toLong())
            if (!widget.params) {
                throw new UnconfiguredWidgetExceptions()
            }
            if (widget.params) {
                config = JSON.parse(widget.params)
            }
            productIds = widget.widgetContent.contentId.collect { it.longValue() }
        } else if (params.url && params.type == "category") {
            config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CATEGORY_PAGE)
            config["show-pagination"] = config["show_pagination"]
            Category category = Category.findByUrlAndIsInTrash(params.url, false)
            productIds = category.products.id
        }
        return [config: config, productIds: productIds]
    }
}

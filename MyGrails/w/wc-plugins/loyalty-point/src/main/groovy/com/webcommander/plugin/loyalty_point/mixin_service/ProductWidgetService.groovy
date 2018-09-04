package com.webcommander.plugin.loyalty_point.mixin_service


class ProductWidgetService {

    def renderLoyaltyPointWidget(Map attrs, Writer writer) {
        renderService.renderView("/plugins/loyalty_point/productWidget/loyaltyPoint", [:], writer)
    }

    def renderLoyaltyPointWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/loyalty_point/productWidget/editor/loyaltyPoint", [:], writer)
    }
}

package com.webcommander.plugin.variation.mixin_service

class ProductWidgetService {

    def renderVariationWidget(Map attrs, Writer writer) {
        renderService.renderView("/plugins/variation/productWidget/variationWidget", [:], writer)
    }

    def renderVariationWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/variation/productWidget/editor/variationWidget", [:], writer)
    }
}

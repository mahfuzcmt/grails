package com.webcommander.plugin.shipment_calculator.mixin_service

/**
 * Created by sajed on 4/20/2014.
 */
class ProductWidgetService {

    def renderShipmentCalculatorWidget(Map attrs, Writer writer) {
        renderService.renderView("/plugins/shipment_calculator/productWidget/shipmentCalculator", [:], writer)
    }

    def renderShipmentCalculatorWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/shipment_calculator/productWidget/editor/shipmentCalculator", [:], writer)
    }
}

package com.webcommander.plugin.shipment_calculator

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ShipmentCalculatorGrailsPlugin extends WebCommanderPluginBase {

    def title = "Shipment Calculator"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Calculate shipping cost'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/shipment-calculator";
    {
        _plugin = new PluginMeta(identifier: "shipment-calculator", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                shippingSetting            : [taglib: "shipmentCalculator", callable: "config"],
                cartDetailsButton          : [taglib: "shipmentCalculator", callable: "calculatorInCart"],
                checkoutAddressSelectorMode: [taglib: "shipmentCalculator", callable: "calculatorInCheckout"],
                siteAddressEditor          : [taglib: "shipmentCalculator", callable: "calculatorInAddressEditor"]
        ]
    }
}

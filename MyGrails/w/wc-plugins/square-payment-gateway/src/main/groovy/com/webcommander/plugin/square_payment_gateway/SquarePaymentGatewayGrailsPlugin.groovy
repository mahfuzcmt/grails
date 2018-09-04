package com.webcommander.plugin.square_payment_gateway

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class SquarePaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "Square Payment Gateway"
    def author = "Zamiur Rahman"
    def authorEmail = "zamiur@bitmascot.com"
    def description = '''SquarePaymentGateway Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/square-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "square-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = []
    }


}

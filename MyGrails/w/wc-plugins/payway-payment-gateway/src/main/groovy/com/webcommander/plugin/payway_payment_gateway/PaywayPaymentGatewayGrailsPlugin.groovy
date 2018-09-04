package com.webcommander.plugin.payway_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class PaywayPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "Payway Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''PaywayPaymentGateway Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/payway-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "payway-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
    }


}

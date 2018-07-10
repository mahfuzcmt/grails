package com.webcommander.plugin.anz_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class AnzPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "ANZ Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through ANZ gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/anz-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "anz-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
    }


}

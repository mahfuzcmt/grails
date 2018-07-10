package com.webcommander.plugin.eway_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class EwayPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "Eway Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through Eway gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/eway-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "eway-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

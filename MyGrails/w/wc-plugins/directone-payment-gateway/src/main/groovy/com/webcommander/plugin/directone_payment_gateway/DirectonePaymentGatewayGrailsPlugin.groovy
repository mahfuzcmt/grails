package com.webcommander.plugin.directone_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class DirectonePaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "DirectOne Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through DirectOne gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/directone-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "directone-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)

    }


}

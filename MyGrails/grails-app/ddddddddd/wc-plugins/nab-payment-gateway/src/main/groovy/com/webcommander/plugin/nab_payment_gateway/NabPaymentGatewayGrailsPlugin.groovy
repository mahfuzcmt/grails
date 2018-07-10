package com.webcommander.plugin.nab_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class NabPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "NAB Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''NabPaymentGateway Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/nab-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "nab-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

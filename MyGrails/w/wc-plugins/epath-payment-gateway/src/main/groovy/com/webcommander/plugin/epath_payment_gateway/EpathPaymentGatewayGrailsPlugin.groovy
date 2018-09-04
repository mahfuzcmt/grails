package com.webcommander.plugin.epath_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class EpathPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "EPath Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through EPath gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/epath-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "epath-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)

    }


}

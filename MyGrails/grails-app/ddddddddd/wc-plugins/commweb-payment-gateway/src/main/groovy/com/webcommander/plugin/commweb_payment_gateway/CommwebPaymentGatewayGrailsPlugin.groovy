package com.webcommander.plugin.commweb_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class CommwebPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "CommWeb Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through CommWeb gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/commweb-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "commweb-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)

    }


}

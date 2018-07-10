package com.webcommander.plugin.braintree_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class BraintreePaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "BrainTree Payment Gateway"
    def author = "Sourav Ahmed"
    def authorEmail = "sourav@bitmascot.com"
    def description = '''Processes payment through Braintree gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/braintree-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "braintree-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)

    }


}

package com.webcommander.plugin.stripe_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class StripePaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "Stripe Payment Gateway"
    def author = "Sourav Ahmed"
    def authorEmail = "sourav@bitmascot.com"
    def description = '''Processes payment through Stripe gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/stripe-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "stripe-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

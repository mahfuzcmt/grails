package com.webcommander.plugin.securepay_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class SecurepayPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {

    def title = "SecurePay Payment Gateway"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Processes payment through SecurePay gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/securepay-payment-gateway";
    {
        _plugin = new PluginMeta(identifier: "securepay-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

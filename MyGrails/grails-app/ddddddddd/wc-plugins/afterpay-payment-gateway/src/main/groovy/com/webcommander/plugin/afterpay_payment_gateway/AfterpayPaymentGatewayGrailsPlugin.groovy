package com.webcommander.plugin.afterpay_payment_gateway

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class AfterpayPaymentGatewayGrailsPlugin extends WebCommanderPluginBase {
    {
        _plugin = new PluginMeta(identifier: "afterpay-payment-gateway", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [productPriceWidget: [taglib: "afterpayPaymentGateway", callable: "productPriceWidget"]]
    }

    def title = "Afterpay Payment Gateway"
    def author = "Hasan Ahmed Khan"
    def authorEmail = "ahmed@bitmascot.com"
    def description = '''Processes payment through AfterPay gateway'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/afterpay-payment-gateway"
}

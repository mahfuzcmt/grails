package com.webcommander.plugin.subscription_from_checkout

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class SubscriptionFromCheckoutGrailsPlugin extends WebCommanderPluginBase {

    def title = "Newsletter Subscription From Checkout"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Ask to subscribe news letter during checkout'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/subscription-from-checkout";
    {
        _plugin = new PluginMeta(identifier: "subscription-from-checkout", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                confirmOrderEnd     : [taglib: "subscriptionApp", callable: "confirmSubscription"],
                checkoutPageConfirmSectionSettings: [taglib: "subscriptionApp", callable: "checkoutPageSettings"]
        ]
    }


}

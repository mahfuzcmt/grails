package com.webcommander.plugin.discount

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class DiscountGrailsPlugin extends WebCommanderPluginBase {

    def title = "Discount"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = ''''''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/discount";
    {
        _plugin = new PluginMeta(identifier: "discount", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        //hooks = []
        hooks = [
            subTotalLeftPanelCartDetails:[taglib: "discount", callable: "couponForm"],
            cartNotificationMessage:[taglib: "discount", callable: "discountMessage"],
            checkoutPaymentOption:[taglib: "discount", callable: "couponFormForCheckPage"],
        ]
    }

}

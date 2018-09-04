package com.webcommander.plugin.gift_wrapper

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants


class GiftWrapperGrailsPlugin extends WebCommanderPluginBase {

    def title = "GiftWrapper"
    def author = "WebCommander Developer"
    def authorEmail = "developer@webcommander.com"
    def description = '''GiftWrapper Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gift-wrapper";
    {
        _plugin = new PluginMeta(identifier: "gift-wrapper", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                adminJss                          : [taglib: "giftWrapperTL", callable: "adminJSs"],
                cartDetailsProductColumn          : [taglib: "giftWrapperTL", callable: "giftWrapperView"],
                confirmStepProductColumn          : [taglib: "giftWrapperTL", callable: "giftWrapperViewForOrderConfirmation"],
                paymentSuccessProductColumn       : [taglib: "giftWrapperTL", callable: "giftWrapperViewForOrdered"],
                pendingOrderDetailsProductColumn  : [taglib: "giftWrapperTL", callable: "giftWrapperViewForOrdered"],
                completedOrderDetailsProductColumn: [taglib: "giftWrapperTL", callable: "giftWrapperViewForOrdered"],
                orderShortDetailsProductNameRow   : [taglib: "giftWrapperTL", callable: "giftWrapperViewForOrderDetailsAdminView"],
                printOrderProductColumn           : [taglib: "giftWrapperTL", callable: "gifadmintWrapperViewForOrderDetailsAdminView"]
        ]
    }

}

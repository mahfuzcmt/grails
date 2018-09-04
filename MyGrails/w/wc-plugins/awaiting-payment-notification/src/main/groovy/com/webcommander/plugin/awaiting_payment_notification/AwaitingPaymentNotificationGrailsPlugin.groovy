package com.webcommander.plugin.awaiting_payment_notification

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class AwaitingPaymentNotificationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Awaiting Payment Notification"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Awaiting Payment Notification"'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/awaiting-payment-notification";
    {
        _plugin = new PluginMeta(identifier: "awaiting-payment-notification", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
    }


}

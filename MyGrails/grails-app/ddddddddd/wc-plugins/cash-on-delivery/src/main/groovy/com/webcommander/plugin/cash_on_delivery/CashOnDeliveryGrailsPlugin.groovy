package com.webcommander.plugin.cash_on_delivery

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class CashOnDeliveryGrailsPlugin extends WebCommanderPluginBase {

    def title = "Cash On Delivery"
    def author = "Sanjoy Kumar Mitra"
    def authorEmail = "sanjoy@bitmascot.com"
    def description = '''Cash On Delivery Payment'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/cash-on-delivery";
    {
        _plugin = new PluginMeta(identifier: "cash-on-delivery", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)

    }


}

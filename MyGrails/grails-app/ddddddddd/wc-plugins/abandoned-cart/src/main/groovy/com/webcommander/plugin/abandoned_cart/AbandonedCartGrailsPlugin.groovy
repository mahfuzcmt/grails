package com.webcommander.plugin.abandoned_cart

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants


class AbandonedCartGrailsPlugin extends WebCommanderPluginBase {

    def title = "Abandoned Cart"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Abandoned Cart'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/abandoned-cart";
    {
        _plugin = new PluginMeta(identifier: "abandoned-cart", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [customerProfileAbandonedCarts: [taglib: "abandonedCart", callable: "customerProfileAbandonedCarts"],customerProfilePluginsJS : [taglib: "abandonedCart", callable: "customerProfilePluginsJS "], customerProfileTabBody: [taglib: "abandonedCart", callable: "customerProfileTabBody"], myAccountPageSettings: [taglib: "abandonedCart", callable: "myAccountPageSetting"]]
    }


}

package com.webcommander.plugin.google_product

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GoogleProductGrailsPlugin extends WebCommanderPluginBase {

    def title = "Google Product"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''GoogleProduct Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/google-product";
    {
        _plugin = new PluginMeta(identifier: "google-product", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

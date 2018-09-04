package com.webcommander.plugin.my_shopping

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class MyShoppingGrailsPlugin extends WebCommanderPluginBase {

    def title = "My Shopping"
    def author = "Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''MyShopping Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/my-shopping";
    {
        _plugin = new PluginMeta(identifier: "my-shopping", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

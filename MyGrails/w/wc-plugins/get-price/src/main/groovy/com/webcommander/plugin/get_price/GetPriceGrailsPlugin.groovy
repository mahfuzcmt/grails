package com.webcommander.plugin.get_price

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class GetPriceGrailsPlugin extends WebCommanderPluginBase {

    def title = "Get Price"
    def author = "Shahin Khaled"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''GetPrice Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/get-price";
    {
        _plugin = new PluginMeta(identifier: "get-price", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

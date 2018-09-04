package com.webcommander.plugin.reorder

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class ReorderGrailsPlugin extends WebCommanderPluginBase {

    def title = "Reorder"
    def author = "Shahin Khaled"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''Reorder completed order from admin panel'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/reorder";
    {
        _plugin = new PluginMeta(identifier: "reorder", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminAddToCartAddedProduct:[taglib:"reorder",callable:"addedProduct"]]
    }


}

package com.webcommander.plugin.standard_variation

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class StandardVariationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Standard Variation"
    def author = "Shahin"
    def authorEmail = "shbahin@bitmascot.com"
    def description = '''Product variation to have support different price and image for different variation'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/standard-variation";
    {
        _plugin = new PluginMeta(identifier: "standard-variation", name: title, dependents: ["variation"], pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:'standard',callable:'adminJss']]
    }


}

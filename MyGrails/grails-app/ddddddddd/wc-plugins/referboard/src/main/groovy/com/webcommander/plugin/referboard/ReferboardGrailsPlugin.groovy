package com.webcommander.plugin.referboard

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ReferboardGrailsPlugin extends WebCommanderPluginBase {

    def title = "Referboard"
    def author = "sajedur"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Referboard integration with webcommander'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/referboard";
    {
        _plugin = new PluginMeta(identifier: "referboard", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[layoutHead:[taglib:"referBoard",callable:"layoutHead"]]
    }


}

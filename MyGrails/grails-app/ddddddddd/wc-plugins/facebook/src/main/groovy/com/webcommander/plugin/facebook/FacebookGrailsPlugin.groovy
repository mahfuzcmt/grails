package com.webcommander.plugin.facebook

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class FacebookGrailsPlugin extends WebCommanderPluginBase {

    def title = "Facebook Integration"
    def author = "Sanjoy Kumar Mitra"
    def authorEmail = "sanjoy@bitmascot.com"
    def description = '''Provides several facebook features for WebCommander'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/facebook";
    {
        _plugin = new PluginMeta(identifier: "facebook", name: title)
        hooks=[layoutHead:[taglib:"fb",callable:"layoutHead"]]
    }


}

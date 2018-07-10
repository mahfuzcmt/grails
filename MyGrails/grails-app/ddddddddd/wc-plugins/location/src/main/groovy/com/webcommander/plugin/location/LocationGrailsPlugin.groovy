package com.webcommander.plugin.location

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class LocationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Location"
    def author = "Anisur Rahman"
    def authorEmail = "anisur@bitmascot.com"
    def description = '''Location Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/location";
    {
        _plugin = new PluginMeta(identifier: "location", name: title)
        hooks = [
                adminJss: [taglib: "location", callable: "adminJSs"]
        ]
    }
}

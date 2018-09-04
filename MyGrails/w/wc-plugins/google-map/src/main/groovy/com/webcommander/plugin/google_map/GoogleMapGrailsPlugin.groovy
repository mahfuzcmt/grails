package com.webcommander.plugin.google_map

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GoogleMapGrailsPlugin extends WebCommanderPluginBase {

    def title = "Google Map Widget"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''GoogleMap Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/google-map";
    {
        _plugin = new PluginMeta(identifier: "google-map", name: title)
        
    }


}

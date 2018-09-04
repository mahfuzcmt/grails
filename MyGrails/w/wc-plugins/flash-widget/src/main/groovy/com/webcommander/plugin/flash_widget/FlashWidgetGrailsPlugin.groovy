package com.webcommander.plugin.flash_widget

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class FlashWidgetGrailsPlugin extends WebCommanderPluginBase {

    def title = "Flash Widget"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Displays flash objects in webcommander page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/flash-widget";
    {
        _plugin = new PluginMeta(identifier: "flash-widget", name: title)
        
    }


}

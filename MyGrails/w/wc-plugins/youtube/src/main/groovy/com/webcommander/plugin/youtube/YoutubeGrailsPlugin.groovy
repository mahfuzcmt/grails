package com.webcommander.plugin.youtube

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class YoutubeGrailsPlugin extends WebCommanderPluginBase {

    def title = "Youtube Widget"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Displays youtube videos in webcommander page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/youtube";
    {
        _plugin = new PluginMeta(identifier: "youtube", name: title)
        
    }


}

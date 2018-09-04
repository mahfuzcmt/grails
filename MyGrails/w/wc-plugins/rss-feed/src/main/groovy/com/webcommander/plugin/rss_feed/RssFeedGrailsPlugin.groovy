package com.webcommander.plugin.rss_feed

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class RssFeedGrailsPlugin extends WebCommanderPluginBase {

    def title = "RSS Feed Widget"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Displays RSS Feed in webcommander page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/rss-feed";
    {
        _plugin = new PluginMeta(identifier: "rss-feed", name: title)
        
    }


}

package com.webcommander.plugin.news

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class NewsGrailsPlugin extends WebCommanderPluginBase {

    def title = "News"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''News Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/news";
    {
        _plugin = new PluginMeta(identifier: "news", name: title)
        
    }


}

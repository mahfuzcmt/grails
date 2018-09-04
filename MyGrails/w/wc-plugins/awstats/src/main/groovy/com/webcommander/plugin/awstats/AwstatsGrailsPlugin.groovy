package com.webcommander.plugin.awstats

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class AwstatsGrailsPlugin extends WebCommanderPluginBase {

    def title = "AWStats"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Displays AWStats in webcommander control panel'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/awstats";
    {
        _plugin = new PluginMeta(identifier: "awstats", name: title)
    }


}

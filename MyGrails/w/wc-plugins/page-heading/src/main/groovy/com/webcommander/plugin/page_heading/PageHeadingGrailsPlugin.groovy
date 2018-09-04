package com.webcommander.plugin.page_heading

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class PageHeadingGrailsPlugin extends WebCommanderPluginBase {

    def title = "Page Heading Widget"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/page-heading";
    {
        _plugin = new PluginMeta(identifier: "page-heading", name: title)
        
    }


}

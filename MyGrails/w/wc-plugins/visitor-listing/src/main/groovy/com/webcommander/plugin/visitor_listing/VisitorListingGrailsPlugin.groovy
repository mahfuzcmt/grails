package com.webcommander.plugin.visitor_listing

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class VisitorListingGrailsPlugin extends WebCommanderPluginBase {

    def title = "Visitor Listing"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Listing all active visitor in Web Commander admin panel'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/visitor-listing";
    {
        _plugin = new PluginMeta(identifier: "visitor-listing", name: title)
        
    }


}

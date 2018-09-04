package com.webcommander.plugin.embedded_page

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class EmbeddedPageGrailsPlugin extends WebCommanderPluginBase {

    def title = "Embedded Page"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''eates and Manages some pages that only contain body and be a subpart of other content'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/embedded-page";
    {
        _plugin = new PluginMeta(identifier: "embedded-page", name: title)

    }


}

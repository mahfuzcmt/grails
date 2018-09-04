package com.webcommander.plugin.tab_accordion

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class TabAccordionGrailsPlugin extends WebCommanderPluginBase {

    def title = "Tab Accordion"
    def author = "Shahin Khaled"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''Make tab or accordion widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/tab-accordion";
    {
        _plugin = new PluginMeta(identifier: "tab-accordion", name: title)
        hooks=[]
        depends = [
                "embedded-page"
        ]
    }


}

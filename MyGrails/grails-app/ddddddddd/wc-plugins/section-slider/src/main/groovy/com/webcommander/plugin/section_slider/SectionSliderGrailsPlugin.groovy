package com.webcommander.plugin.section_slider

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class SectionSliderGrailsPlugin extends WebCommanderPluginBase {

    def title = "Section Slider"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Slides different sections of a page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/section-slider";
    {
        _plugin = new PluginMeta(identifier: "section-slider", name: title)
        hooks=[]
        depends = [
                "embedded-page"
        ]
    }


}

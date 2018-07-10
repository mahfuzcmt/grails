package com.webcommander.plugin.anything_slider

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class AnythingSliderGrailsPlugin extends WebCommanderPluginBase {

    def title = "Anything Slider"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Anything slider for Gallery widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/anything-slider";
    {
        _plugin = new PluginMeta(identifier: "anything-slider", name: title)
        hooks = [adminJss: [taglib: "anythingSlider", callable: "adminJSs"]]
    }


}

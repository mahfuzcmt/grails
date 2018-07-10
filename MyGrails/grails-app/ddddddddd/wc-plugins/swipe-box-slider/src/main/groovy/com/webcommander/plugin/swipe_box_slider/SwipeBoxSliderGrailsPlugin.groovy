package com.webcommander.plugin.swipe_box_slider

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class SwipeBoxSliderGrailsPlugin extends WebCommanderPluginBase {

    def title = "SwipeBox Slider"
    def author = "Shahin"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''SwipeBox slider for Gallery widget'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/swipe-box-slider";
    {
        _plugin = new PluginMeta(identifier: "swipe-box-slider", name: title)
        hooks=[adminJss:[taglib:"swipeSlider",callable:"adminJSs"]]
    }


}

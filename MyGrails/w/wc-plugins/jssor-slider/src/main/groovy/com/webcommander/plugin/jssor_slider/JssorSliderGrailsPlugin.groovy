package com.webcommander.plugin.jssor_slider

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class JssorSliderGrailsPlugin extends WebCommanderPluginBase {

    def title = "Jssor Slider"
    def author = "Shahin Khaled"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''JssorSlider Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/jssor-slider";
    {
        _plugin = new PluginMeta(identifier: "jssor-slider", name: title)
        hooks=[adminJss:[taglib:"jssorSlider",callable:"adminJSs"],albumImagePropertiesTabHeader:[taglib:"jssorSlider",callable:"albumTabHeader"],albumImagePropertiesTabBody:[taglib:"jssorSlider",callable:"albumTabBody"]]
    }


}

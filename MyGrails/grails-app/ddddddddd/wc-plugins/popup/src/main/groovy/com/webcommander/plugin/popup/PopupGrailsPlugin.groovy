package com.webcommander.plugin.popup

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class PopupGrailsPlugin extends WebCommanderPluginBase {

    def title = "Popup"
    def author = "Shahin Khaled"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''Popup Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/popup";
    {
        _plugin = new PluginMeta(identifier: "popup", name: title)
        hooks=[layoutHead:[taglib:"popup",callable:"siteJSs"],sitePageBodyBottom:[taglib:"popup",callable:"renderInitialPopup"]]
    }


}

package com.webcommander.plugin.general_event

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GeneralEventGrailsPlugin extends WebCommanderPluginBase {

    def title = "General Event Management"
    def author = "Arman Bhuiyan"
    def authorEmail = "arman@bitmascot.com"
    def description = '''Manages events'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/general-event";
    {
        _plugin = new PluginMeta(identifier: "general-event", name: title)
        hooks=[adminJss:[taglib:"generalEventApp",callable:"adminJSs"],'admin-css':[taglib:"generalEventApp",callable:"adminCSSs"],eCommerceSettingsTab:[taglib:"generalEventApp",callable:"eCommerceSettings"],checkoutConfirmStep:[taglib:"generalEventApp",callable:"eventCustomField"]]
    }


}

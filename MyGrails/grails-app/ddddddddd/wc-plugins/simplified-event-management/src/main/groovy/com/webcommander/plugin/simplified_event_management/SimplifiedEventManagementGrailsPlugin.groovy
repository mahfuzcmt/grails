package com.webcommander.plugin.simplified_event_management

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class SimplifiedEventManagementGrailsPlugin extends WebCommanderPluginBase {

    def title = "Simplified Event Management"
    def author = "Arman Bhuiyan"
    def authorEmail = "arman@bitmascot.com"
    def description = '''Manages events'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/simplified-event-management";
    {
        _plugin = new PluginMeta(identifier: "simplified-event-management", name: title)
        hooks=[adminJss:[taglib:"simplifiedEventManagementApp",callable:"adminJSs"],'admin-css':[taglib:"simplifiedEventManagementApp",callable:"adminCSSs"],eCommerceSettingsTab:[taglib:"simplifiedEventManagementApp",callable:"eCommerceSettings"],checkoutConfirmStep:[taglib:"simplifiedEventManagementApp",callable:"eventCustomField"]]
    }


}

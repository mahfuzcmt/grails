package com.webcommander.plugin.event_management

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class EventManagementGrailsPlugin extends WebCommanderPluginBase {

    def title = "Event Management"
    def author = "Sabah Binte Noor"
    def authorEmail = "sabah@bitmascot.com"
    def description = '''Manages events and venues'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/event-management";
    {
        _plugin = new PluginMeta(identifier: "event-management", name: title)
        hooks=[adminJss:[taglib:"eventManagementApp",callable:"adminJSs"],'admin-css':[taglib:"eventManagementApp",callable:"adminCSSs"]]
    }


}

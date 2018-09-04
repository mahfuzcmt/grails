package com.webcommander.plugin.google_analytics_reporting

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class GoogleAnalyticsReportingGrailsPlugin extends WebCommanderPluginBase {

    def title = "Google Analytics Reporting"
    def author = "Md. Shajalal"
    def authorEmail = "shajalal@bitmascot.com"
    def description = '''GoogleAnalyticsReporting Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/google-analytics-reporting";
    {
        _plugin = new PluginMeta(identifier: "google-analytics-reporting", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:"google",callable:"adminJSs"]]
    }


}

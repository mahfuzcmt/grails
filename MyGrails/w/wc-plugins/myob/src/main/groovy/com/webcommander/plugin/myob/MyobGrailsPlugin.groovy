package com.webcommander.plugin.myob

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class MyobGrailsPlugin extends WebCommanderPluginBase {

    def title = "MYOB Integration"
    def author = "Sanjoy Kumar Mitra"
    def authorEmail = "sanjoy@bitmascot.com"
    def description = '''Myob Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/myob";
    {
        _plugin = new PluginMeta(identifier: "myob", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

package com.webcommander.plugin.xero

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class XeroGrailsPlugin extends WebCommanderPluginBase {

    def title = "Xero Integration"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Syncs Xero products/customers with WebCommander'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/xero";
    {
        _plugin = new PluginMeta(identifier: "xero", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

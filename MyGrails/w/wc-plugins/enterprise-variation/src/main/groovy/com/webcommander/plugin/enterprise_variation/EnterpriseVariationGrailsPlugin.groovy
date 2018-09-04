package com.webcommander.plugin.enterprise_variation

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class EnterpriseVariationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Enterprise Variation"
    def author = "Shahin"
    def authorEmail = "shahin@bitmascot.com"
    def description = '''A product variation where every variants will have their own properties'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/enterprise-variation";
    {
        _plugin = new PluginMeta(identifier: "enterprise-variation", name: title, dependents: ["variation"], pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:'enterprise', callable:'adminJss']]
    }


}

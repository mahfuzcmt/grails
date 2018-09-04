package com.webcommander.plugin.filter

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class FilterGrailsPlugin extends WebCommanderPluginBase {

    def title = "Filter"
    def author = "Shajalal"
    def authorEmail = "shajalal@bitmascot.com"
    def description = '''Manage filters'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/filter";
    {
        _plugin = new PluginMeta(identifier: "filter", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[
                filterJss:[taglib:"filter",callable:"filterJss"],
                filterProfileSelectInCategoryBasic:[taglib:"filter",callable:"addFilterProfileInCategory"],
                saveCategoryInFilterProfile:[bean:"filterService",callable:"saveFilterProfileForCategory"],
                productEditorTabHeader:[taglib:"filter",callable:"addToProductEditor"],
        ]
    }


}

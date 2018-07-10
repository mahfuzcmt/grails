package com.webcommander.plugin.pos

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class PosGrailsPlugin extends WebCommanderPluginBase {

    def title = "POS"
    def author = "Md Sajedur Rahman "
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Pos Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/pos";
    {
        _plugin = new PluginMeta(identifier: "pos", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[]
    }


}

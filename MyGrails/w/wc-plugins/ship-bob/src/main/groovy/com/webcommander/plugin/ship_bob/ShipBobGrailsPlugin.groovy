package com.webcommander.plugin.ship_bob

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ShipBobGrailsPlugin extends WebCommanderPluginBase {

    def title = "ShipBob"
    def author = "Sajid"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Shipbob integration with webcommander'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/ship-bob";
    {
        _plugin = new PluginMeta(identifier: "ship-bob", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        
    }


}

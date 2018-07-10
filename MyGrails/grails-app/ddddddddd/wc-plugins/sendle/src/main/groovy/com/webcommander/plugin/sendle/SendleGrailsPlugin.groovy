package com.webcommander.plugin.sendle

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class SendleGrailsPlugin extends WebCommanderPluginBase {

    def title = "Sendle"
    def author = "Zamiur Rahman"
    def authorEmail = "zamiur@bitmascot.com"
    def description = '''Sendle Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/sendle";
    {
        _plugin = new PluginMeta(identifier: "sendle", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [ addShipmentBlock: [taglib: "sendle", callable: "addShipmentBlock"]]
    }


}

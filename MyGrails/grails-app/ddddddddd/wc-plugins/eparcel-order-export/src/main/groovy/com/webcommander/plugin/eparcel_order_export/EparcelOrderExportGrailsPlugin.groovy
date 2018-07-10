package com.webcommander.plugin.eparcel_order_export

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class EparcelOrderExportGrailsPlugin extends WebCommanderPluginBase {

    def title = "Eparcel Order Export"
    def author = "Akter Hossain"
    def authorEmail = "akter@bitmascot.com"
    def description = '''Eparcel Order Export'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/eparcel-order-export";
    {
        _plugin = new PluginMeta(identifier: "eparcel-order-export", name: title)
        hooks = [adminJss: [taglib: 'eparcel', callable: 'adminJss']]
    }


}

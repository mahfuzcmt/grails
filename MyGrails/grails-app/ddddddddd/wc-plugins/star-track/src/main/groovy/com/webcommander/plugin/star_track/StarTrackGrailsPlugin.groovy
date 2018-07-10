package com.webcommander.plugin.star_track

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class StarTrackGrailsPlugin extends WebCommanderPluginBase {

    def title = "Star Track"
    def author = "Md Sajedur Rahman "
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Calculate shipping cost from star track'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/star-track";
    {
        _plugin = new PluginMeta(identifier: "star-track", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[shippingRateApiBlock:[taglib:'starTrack',callable:'shippingRateApiBlock']]
    }


}

package com.webcommander.plugin.quote

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class QuoteGrailsPlugin extends WebCommanderPluginBase {

    def title = "Quote"
    def author = "Md Sajedur Rahman "
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Get quote feature'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/quote";
    {
        _plugin = new PluginMeta(identifier: "quote", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[cartDetailsButton:[taglib:"quote",callable:"getQuoteButtonInCart"]]
    }


}

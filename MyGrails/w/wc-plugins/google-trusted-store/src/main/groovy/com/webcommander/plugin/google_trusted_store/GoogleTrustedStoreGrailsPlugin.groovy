package com.webcommander.plugin.google_trusted_store

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class GoogleTrustedStoreGrailsPlugin extends WebCommanderPluginBase {

    def title = "Google Trusted Store "
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''GoogleTrustedStore Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/google-trusted-store";
    {
        _plugin = new PluginMeta(identifier: "google-trusted-store", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[layoutHead:[taglib:"googleTrustedStore",callable:"layoutHead"],paymentSuccessAfterTable:[taglib:"googleTrustedStore",callable:"confirmStep"]]
    }


}

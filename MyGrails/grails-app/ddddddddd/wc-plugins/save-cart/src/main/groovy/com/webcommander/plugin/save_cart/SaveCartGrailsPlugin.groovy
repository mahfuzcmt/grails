package com.webcommander.plugin.save_cart

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class SaveCartGrailsPlugin extends WebCommanderPluginBase {

    def title = "Save Cart"
    def author = "Md Sajedur Rahman "
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''Save cart'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/save-cart";
    {
        _plugin = new PluginMeta(identifier: "save-cart", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[cartDetailsButton:[taglib:"saveCart",callable:"saveCartButtonInCart"],customerProfileSaveCarts:[taglib:"saveCart",callable:"customerProfileSaveCarts"],customerProfilePluginsJS:[taglib:"saveCart",callable:"customerProfilePluginsJS"],customerProfileTabBody:[taglib:"saveCart",callable:"customerProfileTabBody"],myAccountPageSettings:[taglib:"saveCart",callable:"myAccountPageSetting"]]
    }


}

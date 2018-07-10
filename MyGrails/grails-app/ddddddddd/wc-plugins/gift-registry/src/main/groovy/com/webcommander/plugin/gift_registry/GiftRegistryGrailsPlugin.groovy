package com.webcommander.plugin.gift_registry

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class GiftRegistryGrailsPlugin extends WebCommanderPluginBase {

    def title = "Gift Registry"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''GiftRegistry Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gift-registry";
    {
        _plugin = new PluginMeta(identifier: "gift-registry", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[productCartBlock:[taglib:"giftRegistry",callable:"addToGiftRegistry"],productCartBlockForEditor:[taglib:"giftRegistry",callable:"addToGiftRegistryForEditor"],productImageViewPriceBlock:[taglib:"giftRegistry",callable:"renderGiftItemData"],customerProfileGiftRegistry:[taglib:"giftRegistry",callable:"customerProfileGiftRegistry"],customerProfilePluginsJS:[taglib:"giftRegistry",callable:"customerProfilePluginsJS"],customerProfileTabBody:[taglib:"giftRegistry",callable:"customerProfileTabBody"],productPageConfigurationForm:[taglib:"giftRegistry",callable:"productPageConfig"],myAccountPageSettings:[taglib:"giftRegistry",callable:"myAccountPageSetting"]]
    }


}

package com.webcommander.plugin.ebay_listing

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class EbayListingGrailsPlugin extends WebCommanderPluginBase {

    def title = "Ebay Listing"
    def author = "Akter Hossain"
    def authorEmail = "akter@bitmascot.com"
    def description = '''Sync WebCommander with Ebay Listing'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/ebay-listing";
    {
        _plugin = new PluginMeta(identifier: "ebay-listing", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:'ebayListing',callable:'adminJss'],productEditorTabHeader:[taglib:"ebayListing",callable:"addToProductEditor"],productEditorTabBody:[taglib:"ebayListing",callable:"productEditorTabBody"],categoryEditorTabHeader:[taglib:"ebayListing",callable:"categoryEditorTabHeader"],categoryEditorTabBody:[taglib:"ebayListing",callable:"categoryEditorTabBody"],]
    }


}

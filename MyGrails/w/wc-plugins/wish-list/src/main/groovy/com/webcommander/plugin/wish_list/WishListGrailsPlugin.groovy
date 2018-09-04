package com.webcommander.plugin.wish_list

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class WishListGrailsPlugin extends WebCommanderPluginBase {

    def title = "Wish List"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Adds support to manage wishlist for customer'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/wish-list";
    {
        _plugin = new PluginMeta(identifier: "wish-list", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[productCartBlock:[taglib:"wishList",callable:"addToWishList"],productCartBlockForEditor:[taglib:"wishList",callable:"addToWishListForEditor"],customerProfilePageOverviewSettings:[taglib:"wishList",callable:"customerProfilePageOverviewSettings"],productListPriceCol:[taglib:"wishList",callable:"addToWishList"],productImageViewPriceBlock:[taglib:"wishList",callable:"addToWishList"],customerProfileWishLists:[taglib:"wishList",callable:"customerProfileWishLists"],customerProfilePluginsJS:[taglib:"wishList",callable:"customerProfilePluginsJS"],customerProfileTabBody:[taglib:"wishList",callable:"customerProfileTabBody"],productWidgetConfig:[taglib:"wishList",callable:"productWidgetConfig"],productPageConfigurationForm:[taglib:"wishList",callable:"productPageConfig"],configProductInCategoryPage:[taglib:"wishList",callable:"categoryPageConfig"],configSearchPage:[taglib:"wishList",callable:"configSearchPage"],myAccountPageSettings:[taglib:"wishList",callable:"myAccountPageSetting"]]
    }


}

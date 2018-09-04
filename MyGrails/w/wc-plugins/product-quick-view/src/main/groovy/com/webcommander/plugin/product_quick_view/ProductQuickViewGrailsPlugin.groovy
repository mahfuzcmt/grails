package com.webcommander.plugin.product_quick_view

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ProductQuickViewGrailsPlugin extends WebCommanderPluginBase {

    def title = "Product Quick View"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''by click on a product open a product details popup'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/product-quick-view";
    {
        _plugin = new PluginMeta(identifier: "product-quick-view", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=['siteJSs':[taglib:"quickViewSpace",callable:"siteJs"]]
    }


}

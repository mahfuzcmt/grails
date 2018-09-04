package com.webcommander.plugin.auto_product_load_on_scroll

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class AutoProductLoadOnScrollGrailsPlugin extends WebCommanderPluginBase {

    def title = "Auto Loads More Product On Scroll"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Load more product when scrollbar is reached at the end of the product page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/auto-product-load-on-scroll";
    {
        _plugin = new PluginMeta(identifier: "auto-product-load-on-scroll", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = ['siteJSs': [taglib: "productAutoScroll", callable: "siteJs"], configProductInCategoryPage: [taglib: "productAutoScroll", callable: "loadCategorySettings"], productWidgetConfigurationPanel: [taglib: "productAutoScroll", callable: "loadWidgetSettings"]]
    }


}

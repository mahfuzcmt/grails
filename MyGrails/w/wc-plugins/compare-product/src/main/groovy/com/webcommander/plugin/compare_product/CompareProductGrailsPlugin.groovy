package com.webcommander.plugin.compare_product

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class CompareProductGrailsPlugin extends WebCommanderPluginBase {

    def title = "Compare Product"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Compare product specification in webcommander page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/compare-product";
    {
        _plugin = new PluginMeta(identifier: "compare-product", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:"productCompare",callable:"adminJSs"],productImageViewPriceBlock:[taglib:"productCompare",callable:"addToCompare"],productListPriceCol:[taglib:"productCompare",callable:"addToCompare"],customerProfileTabHeader:[taglib:"productCompare",callable:"customerProfileTabHeader"],productEditorTabHeader:[taglib:"productCompare",callable:"addToProductEditor"],productWidgetConfig:[taglib:"productCompare",callable:"productWidgetConfig"],productPageConfigurationForm:[taglib:"productCompare",callable:"productPageConfig"],productCartBlock:[taglib:"productCompare",callable:"addToCompare"],productCartBlockForEditor:[taglib:"productCompare",callable:"addToCompareForEditor"],configProductInCategoryPage:[taglib:"productCompare",callable:"categoryPageConfig"],configSearchPage:[taglib:"productCompare",callable:"configSearchPage"]]
    }


}

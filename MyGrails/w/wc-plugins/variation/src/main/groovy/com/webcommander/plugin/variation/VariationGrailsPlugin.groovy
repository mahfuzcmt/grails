package com.webcommander.plugin.variation

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class VariationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Product Variation"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Gives support to have multiple variants for a product'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/variation";
    {
        _plugin = new PluginMeta(identifier: "variation", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [adminJss                         : [taglib: "variation", callable: "adminJSs"],
                 layoutHead                       : [taglib: "variation", callable: "siteJSs"],
                 productEditorTabHeader           : [taglib: "variation", callable: "productEditorTabHeader"],
                 productPageConfigurationForm     : [taglib: "variation", callable: "productPageConfig"],
                 addCartPopup                     : [taglib: "variation", callable: "variationPopup"],
                 productShortViewContribution     : [taglib: "variation", callable: "variationWidget"],
                 productWidgetViewAfterPriceColumn: [taglib: "variation", callable: "variationSelectionInProductList"],
                 googleProductConfigContribution  : [taglib: "variation", callable: "googleProductConfig"],
                 configProductInCategoryPage      : [taglib: "variation", callable: "variationConfigView"],
                 productWidgetConfig              : [taglib: "variation", callable: "productWidgetConfig"]]
    }


}

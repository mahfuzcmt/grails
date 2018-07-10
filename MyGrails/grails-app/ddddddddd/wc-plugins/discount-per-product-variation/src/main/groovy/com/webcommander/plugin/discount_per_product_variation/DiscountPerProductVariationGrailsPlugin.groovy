package com.webcommander.plugin.discount_per_product_variation

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class DiscountPerProductVariationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Discount on per product variation"
    def author = "Tariq Ahmed Khan"
    def authorEmail = "tariq@bitmascot.com"
    def description = '''Gives support to show and apply discount on product variations'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/discount-per-product-variation";
    {
        _plugin = new PluginMeta(identifier: "discount-per-product-variation", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [productSelectionColumn: [taglib: "discountProductVariation", callable: "productSelectionColumn"], discountSave: [bean: "variationDiscountService", callable: "saveVariationInfo"], productDiscountCalculation: [bean: "variationDiscountService", callable: "getSelectedVariationInfo"]]
        depends = ["discount", "variation"]
    }


}

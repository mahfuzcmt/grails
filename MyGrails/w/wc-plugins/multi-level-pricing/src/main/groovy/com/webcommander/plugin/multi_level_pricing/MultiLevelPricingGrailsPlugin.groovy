package com.webcommander.plugin.multi_level_pricing

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class MultiLevelPricingGrailsPlugin extends WebCommanderPluginBase {

    def title = "MultiLevelPricing"
    def author = "Imrul Hasan"
    def authorEmail = "imrul@bitmascot.com"
    def description = '''MultiLevelPricing Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/multi-level-pricing";
    {
        _plugin = new PluginMeta(identifier: "multi-level-pricing", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
            eCommerceSettingsTab: [taglib: "multiLevelPricing", callable: "multiLevelPricingSetting"],
            productEditPriceAndStock: [taglib: "multiLevelPricing", callable: "addMultiLevelPricingForEachProduct"],
            priceBlockContainer: [taglib: "multiLevelPricing", callable: "priceBlockContainer"],
            productPriceWidget: [taglib: "multiLevelPricing", callable: "priceBlockContainer"],
            incompleteDataCartPopup: [taglib: "multiLevelPricing", callable: "incompleteDataCartPopup"],
        ]
    }


}

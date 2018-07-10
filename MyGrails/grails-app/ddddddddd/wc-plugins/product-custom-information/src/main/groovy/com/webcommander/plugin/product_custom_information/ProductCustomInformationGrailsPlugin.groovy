package com.webcommander.plugin.product_custom_information

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ProductCustomInformationGrailsPlugin extends WebCommanderPluginBase {

    def title = "Product Custom Information"
    def author = "Tariq Ahmed Khan"
    def authorEmail = "tariq@bitmascot.com"
    def description = '''Show Custom Information of Product'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/product-custom-information";
    {
        _plugin = new PluginMeta(identifier: "product-custom-information", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[productPageSettings:[taglib:"productCustomInfo",callable:"productPageSettings"],productAdvancedInformation:[taglib:"productCustomInfo",callable:"productAdvancedInformation"],saveCategoryAdvancedData:[bean:"productCustomInformationService",callable:"saveCustomInformationValue"],productInfoTabHeader:[taglib:"productCustomInfo",callable:"productTabInformationHeader"],productInfoTabBody:[taglib:"productCustomInfo",callable:"productTabInformationBody"]]
    }


}

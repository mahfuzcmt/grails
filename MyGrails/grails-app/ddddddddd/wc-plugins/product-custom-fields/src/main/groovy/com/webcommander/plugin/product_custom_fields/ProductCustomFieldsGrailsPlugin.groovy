package com.webcommander.plugin.product_custom_fields

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ProductCustomFieldsGrailsPlugin extends WebCommanderPluginBase {

    def title = "ProductCustomFields"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Adds some custom fields in product to carry information in checkout process'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/product-custom-fields";
    {
        _plugin = new PluginMeta(identifier: "product-custom-fields", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:"productField",callable:"adminJSs"],productEditorTabHeader:[taglib:"productField",callable:"productEditorTabHeader"],productEditorTabBody:[taglib:"productField",callable:"productEditorTabBody"],categoryEditorTabHeader:[taglib:"productField",callable:"categoryEditorTabHeader"],categoryEditorTabBody:[taglib:"productField",callable:"categoryEditorTabBody"],addCartPopup:[taglib:"productField",callable:"addCartPopup"],variationsForCartAdd:[bean:"productCustomFieldService",callable:"variationsForCartAdd"]]
    }


}

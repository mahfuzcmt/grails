package com.webcommander.plugin.order_custom_fields

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class OrderCustomFieldsGrailsPlugin extends WebCommanderPluginBase {

    def title = "Order Custom Fields"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''OrderCustomFields Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/order-custom-fields";
    {
        _plugin = new PluginMeta(identifier: "order-custom-fields", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks=[adminJss:[taglib:"orderFields",callable:"adminJSs"],checkoutConfirmStep:[taglib:"orderFields",callable:"customFields"],orderDetailsRowEnd:[taglib:"orderFields",callable:"loadDetailsView"]]
    }


}

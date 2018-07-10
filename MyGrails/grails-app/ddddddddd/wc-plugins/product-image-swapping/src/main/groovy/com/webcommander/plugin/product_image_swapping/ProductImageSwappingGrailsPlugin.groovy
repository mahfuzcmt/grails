package com.webcommander.plugin.product_image_swapping

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class ProductImageSwappingGrailsPlugin extends WebCommanderPluginBase {

    def title = "Product Image Swapping"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Swap between 1st and 2nd product images in product widget views'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/product-image-swapping";
    {
        _plugin = new PluginMeta(identifier: "product-image-swapping", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                'siteJSs'          : [taglib  : "imageSwappingApp", callable: "siteJs"],
                imageBlockInProductImageView: [taglib  : "imageSwappingApp", callable: "productImageView"]
        ]
    }


}

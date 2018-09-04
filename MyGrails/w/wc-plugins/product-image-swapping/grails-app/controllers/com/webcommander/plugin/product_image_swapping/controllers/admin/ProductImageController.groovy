package com.webcommander.plugin.product_image_swapping.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class ProductImageController {
    def loadProductImageSettingView() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT_IMAGE_SWAPPING)
        render (view: "/plugins/product_image_swapping/admin/setting/productImageSetting", model: [config: config])
    }
}

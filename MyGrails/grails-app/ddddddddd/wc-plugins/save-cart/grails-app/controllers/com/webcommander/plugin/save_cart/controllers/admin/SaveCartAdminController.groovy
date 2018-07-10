package com.webcommander.plugin.save_cart.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class SaveCartAdminController {

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SAVE_CART);
        render(view: "/plugins/save_cart/admin/appConfig", model: [config: config])
    }
}

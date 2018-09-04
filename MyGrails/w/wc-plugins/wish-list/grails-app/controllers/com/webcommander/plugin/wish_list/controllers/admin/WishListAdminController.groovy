package com.webcommander.plugin.wish_list.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class WishListAdminController {
    def loadConfig() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.WISH_LIST)
        render (view: "/plugins/wish_list/admin/config", model: [config: config])
    }
}

package com.webcommander.plugin.gift_registry.controllers.site.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil


class GiftRegistryAdminController {

    def loadConfig() {
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_REGISTRY)
        render (view: "/plugins/gift_registry/admin/config", model: [config: config])
    }

}

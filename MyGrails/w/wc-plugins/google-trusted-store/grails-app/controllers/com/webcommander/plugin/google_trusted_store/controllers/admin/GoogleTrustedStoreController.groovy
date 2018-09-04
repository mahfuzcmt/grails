package com.webcommander.plugin.google_trusted_store.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class GoogleTrustedStoreController {
    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_TRUSTED_STORE)
        render(view: "/plugins/google_trusted_store/admin/setting/loadConfig", model: [config: config])
    }
}

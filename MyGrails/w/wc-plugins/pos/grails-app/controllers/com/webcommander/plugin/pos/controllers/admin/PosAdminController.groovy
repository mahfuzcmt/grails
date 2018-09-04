package com.webcommander.plugin.pos.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil


class PosAdminController {

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.POS);
        render(view: "/plugins/pos/admin/appConfig", model: [config: config])
    }
}
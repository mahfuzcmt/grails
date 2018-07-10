package com.webcommander.plugin.referboard.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class ReferboardController {
    @License(required = "allow_referboard_feature")
    def config() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERBOARD);
        render(view: "/plugins/referboard/admin/config", model: [configs: configs])
    }
}

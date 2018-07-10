package com.webcommander.controllers.rest.admin.config

import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor

class SettingController extends RestProcessor {
    def fetchConfigs() {
        Map configs = AppUtil.getConfig(params.type)
        rest configs
    }
}
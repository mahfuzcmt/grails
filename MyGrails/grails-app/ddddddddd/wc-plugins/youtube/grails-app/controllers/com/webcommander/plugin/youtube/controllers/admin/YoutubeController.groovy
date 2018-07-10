package com.webcommander.plugin.youtube.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import grails.converters.JSON

class YoutubeController {

    def config() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.YOUTUBE);
        render(view: "/plugins/youtube/admin/config", model: [configs: configs])
    }

    def fetchConfig() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.YOUTUBE);
        render(configs as JSON)
    }
}

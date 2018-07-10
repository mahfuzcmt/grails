package com.webcommander.plugin.facebook.controllers

import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import grails.converters.JSON

class FacebookController {
    ConfigService configService

    @RequiresAdmin
    def config(){
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FACEBOOK);
        render(view: "/plugins/facebook/admin/config", model: [config: config])
    }

    @RequiresAdmin
    def saveConfigs(){
        def configs = [];
        params.list("type").each {type ->
            params."${type}".each{
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        if (configService.update(configs)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }
}

package com.webcommander.controllers.admin

import com.webcommander.admin.CloudStorageService
import com.webcommander.admin.ConfigService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CacheManager
import com.webcommander.util.AppUtil

class CloudStorageController {
    CloudStorageService cloudStorageService = new CloudStorageService()
    ConfigService configService = new ConfigService()

    def removeCache() {
        CacheManager.removeCache("global", "config", DomainConstants.SITE_CONFIG_TYPES.AWS_S3)
        render("Cache removed !")
    }

    def loadAppView() {
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CLOUD_STORAGE)
        render(view: "/admin/setting/cloudStorageSetting", model: [configs: configs])
    }

    def saveConfigs() {
        def configs = []
        params.list("type").each { type ->
            params."${type}".each {
                configs.add([type: type, configKey: it.key, value: it.value]);
            }
        }
        if (configService.update(configs)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as grails.converters.JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as grails.converters.JSON)
        }
//        Map config = params ? params["cloud-storage"]?."aws" : []
//        if(config.is_enabled) {
//            boolean flag = false
//            if(config.enable_resource_bucket) {
//                flag = cloudStorageService.saveAWSConfig(config["resource"], "resource")
//            }
//            if(config.enable_public_bucket) {
//                flag = cloudStorageService.saveAWSConfig(config["public"], "public")
//            }
//            if (flag) {
//                render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
//            } else {
//                render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
//            }
//        }
    }
}

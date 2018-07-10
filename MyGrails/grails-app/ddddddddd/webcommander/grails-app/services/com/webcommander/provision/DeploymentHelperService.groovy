package com.webcommander.provision

import com.webcommander.admin.UserService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.plugin.PluginManager


class DeploymentHelperService {

    TemplateInstallationService templateInstallationService
    UserService userService

    def installPlugins(List<String> plugins) {
        PluginManager.activatePluginFromList(plugins)
    }

    def installPluginAndTemplate(def params) {
        List<String> plugins = params.pluginNames
        Map templateParams = [:]
        Map templateDetails = [:]
        templateParams.id = params.templateUUID
        templateParams.color = params.color
        templateDetails.name = params.templateName
        templateDetails.liveURL = params.liveURL
        templateDetails.uud = params.templateUUID
        if (plugins){
            PluginManager.activatePluginFromList(plugins)
        }
        if (templateDetails.liveURL){
            templateInstallationService.install(templateParams, templateDetails)
        }
    }

    def getDeploymentOperator(def params){
        return userService.getUsers(params).first()
    }


}

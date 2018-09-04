package com.webcommander.controllers.admin.plugin

import com.webcommander.admin.LicenseService
import com.webcommander.admin.PluginService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.PluginMeta
import com.webcommander.plugin.PluginManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.tenant.Thread
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders

class PluginController {

    CommonService commonService
    PluginService pluginService
    LicenseService licenseService
    ProvisionAPIService provisionAPIService

    @Restriction(permission = "plugin.view.list")
    def loadAppView() {
        render view: "/admin/plugin/appView"
    }

    def myPackagePlugins() {
        render view: "/admin/plugin/myPackagePlugins", model: pluginService.getInstallablePlugin()
    }

    def allPackagePlugins() {
        def pluginByPackage = [] //provisionAPIService.getAllPackageWithPlugin()
        def addons =[] // provisionAPIService.getAllAddon()
        Map licenseConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE)
        licenseConfig.package_weight = licenseConfig.package_weight ? licenseConfig.package_weight.toInteger() : 0
        render view: "/admin/plugin/allPackagePlugins", model: [pluginByPackage: pluginByPackage, licenseConfig: licenseConfig, addons: addons]
    }

    def advanceFilter() {
        render(view: "/admin/plugin/filter", model: [d: true])
    }

    @Restriction(permission = "plugin.install")
    def install() {
//        if (Holders.config.webcommander.provision.enabled) {
//            Map confirmData = provisionAPIService.pluginConfirmation(params.identifier)
//            if (!confirmData.isAbleToInstall && !confirmData.isAddon) {
//                render([status: "error", message: confirmData.message] as JSON)
//                return
//            }
//        }
        Boolean result = pluginService.install(params)
        if (result) {
            licenseService.refresh()
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "plugin.installation.failed")] as JSON)
        }
    }

    @Restriction(permission = "plugin.uninstall")
    def uninstall() {
        PluginMeta meta = PluginManager.activePlugins.find { it.identifier == params.id }
        List childPlugins = PluginManager.activePlugins.findAll { it.dependents.contains(meta.identifier) }
        if (!params.uninstall_dependent && childPlugins && childPlugins.size()) {
            String dependentPlugin = childPlugins.name.join(", ")
            render([status: "alert", message: g.message(code: "can.not.uninstall.plugin", args: [meta.name, dependentPlugin])] as JSON)
            return
        }
        if (PluginManager.isSoftInstallUninstallEnable()) {
            pluginService.deActivate(params.id)
            licenseService.refresh()
        } else {
            pluginService.uninstall(params.id)
        }
        render([status: "success"] as JSON)
    }

    @Restriction(permission = "plugin.restart.server")
    def restart() {
        Thread.start {
            Thread.sleep(500)
            try {
                provisionAPIService.restart()
            } catch (Exception ex) {
                log.debug(ex)
            }
        }
        render([status: "success"] as JSON)
    }
}

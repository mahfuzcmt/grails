package com.webcommander.plugin.popup

import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.ConfigService
import com.webcommander.admin.Role
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.manager.HookManager
import com.webcommander.models.TemplateData
import com.webcommander.tenant.TenantContext
import com.webcommander.util.DomainUtil
import com.webcommander.util.PluginDestroyUtil

class BootStrap {

    private static final POPUP = "popup"

    Map siteConfigs = [
            initial_popup: "",
            loading_frequency: "always",
            config_version: "0"
    ]

    List domain_constants = [
            [constant:"SITE_CONFIG_TYPES", key: "POPUP", value: POPUP]
    ]

    List permissions =  [
            ["edit", true], ["remove", true], ["create", false], ["view.list", false], ["edit.permission", false]
    ]

    ConfigService configService

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        ConfigService.addTab(POPUP, [
                url: "popupAdmin/loadConfig",
                message_key: "popup"
        ])

        if(SiteConfig.findByType(POPUP) == null) {
            siteConfigs.each {
                new SiteConfig(type: POPUP, configKey: it.key, value: it.value).save()
            }
        }

        if(!Permission.findByType(POPUP)) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "popup").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        ConfigService.removeTab(POPUP)
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removePermission(POPUP)
            util.removeSiteConfig(POPUP)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin popup From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
        DomainConstants.removeConstant(domain_constants)
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register("provide-template-data", { TemplateData data ->
            data.otherContents["popups"] = Popup.list().collect {
                DomainUtil.toMap(it)
            }
            data.collectSiteConfig("popup")
        });

        AppEventManager.on("copy-template-data", { TemplateData data, InstallationDataHolder holder ->
            List<Map> popups = data.otherContents["popups"] ?: []
            popups.each {
                Popup popup = new Popup()
                it.contentId = holder.getContentMapping("snippet", it.contentId, "id")
                DomainUtil.populateDomainInst(popup, it)
                popup.save()
                holder.setContentMapping("popup", it.id, "id", popup.id)
            }
            String initPopup = holder.getContentMapping("popup", data.siteConfigs["popup"]?.initial_popup, "id")
            if(initPopup) {
                configService.update([[type: "popup", configKey: "initial_popup", value: initPopup]])
            }
        })
    }

    def destroy = {
    }
}
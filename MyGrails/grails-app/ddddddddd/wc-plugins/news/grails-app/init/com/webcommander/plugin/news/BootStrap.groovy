package com.webcommander.plugin.news

import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.DisposableUtilService
import com.webcommander.admin.Role
import com.webcommander.admin.RoleService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.HookManager
import com.webcommander.models.RestrictionPolicy
import com.webcommander.plugin.news.mixin_service.WidgetService as NWS
import com.webcommander.plugin.news.mixin_service.TemplateInstallationService as TIS
import com.webcommander.plugin.news.mixin_service.TemplateDataProviderService as TDPS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import grails.util.Holders

class BootStrap {

    private static final NEWS = "news"

    List permissions = [["edit", true], ["create", false], ["view.list", false], ["remove", true], ["edit.permission", false]]

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "NEWS", value: NEWS],
            [constant:"WIDGET_CONTENT_TYPE", key: "NEWS", value: NEWS]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: NEWS + ".title", value: "news.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: NEWS + ".label", value: "news"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        if (!Permission.findByType(NEWS)) {
            Role role = Role.findByName("Admin")
            permissions.each { entry ->
                Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: "news").save()
                new RolePermission(role: role, permission: permission, isAllowed: true).save()
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removePermission(NEWS)
            util.removeWidget(NEWS)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin news From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin NWS
        Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).metaClass.mixin TIS
        Holders.grailsApplication.mainContext.getBean(TemplateDataProviderService).metaClass.mixin TDPS
        Holders.grailsApplication.mainContext.getBean(DisposableUtilService)
                .putDisposableUtilFactory(NEWS, Holders.grailsApplication.mainContext.getBean(NewsService))

        TenantContext.eachParallelWithWait(tenantInit)
        AppEventManager.on("news-update", { id ->
            TemplateContent.where {
                contentType == NEWS
                contentId == id
            }.deleteAll()
        })
        HookManager.register("beforeManageUserPermission beforeSaveUserPermission", { Map response, Map params ->
            if (params.type == "news") {
                Long admin = AppUtil.session.admin
                response.deniedPolicy = new RestrictionPolicy(type: "news", permission: "edit.permission")
                response.allowed = RoleService.getInstance().isPermitted(admin, response.deniedPolicy, params)
            }
            return response
        })
    }

    def destroy = {
    }
}

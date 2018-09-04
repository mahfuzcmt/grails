package com.webcommander.plugin.snippet

import com.webcommander.AppResourceTagLib
import com.webcommander.acl.Permission
import com.webcommander.acl.RolePermission
import com.webcommander.admin.DisposableUtilService
import com.webcommander.admin.Role
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.DockSection
import com.webcommander.design.Layout
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.HookManager
import com.webcommander.plugin.snippet.mixin_service.WidgetService as WS
import com.webcommander.plugin.snippet.mixin_service.TemplateDataProviderService as TDPS
import com.webcommander.plugin.snippet.mixin_service.TemplateInstallationService as TIS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.util.Holders
import groovy.io.FileType

class BootStrap {
    WidgetService widgetService
    SnippetService snippetService

    private final String SNIPPET = "snippet"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "SNIPPET", value: SNIPPET],
            [constant:"WIDGET_CONTENT_TYPE", key: "SNIPPET", value: SNIPPET]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: SNIPPET + ".title", value:"snippet.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: SNIPPET + ".label", value:SNIPPET],
            [constant:"WIDGET_LICENSE", key: SNIPPET, value:"allow_snippet_feature"]
    ]

    Map permissions = [
            snippet: [
                    ["edit", true], ["remove", true], ["create", false], ["view.list", false], ["edit.permission", false]
            ],
            snippet_template: [
                    ["edit", false], ["remove", false], ["create", false], ["view.list", false], ["edit.permission", false]
            ]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        permissions.each {type, perm ->
            if (!Permission.findByType(type)) {
                Role role = Role.findByName("Admin")
                perm.each { entry ->
                    Permission permission = new Permission(name: entry[0], label: entry[0], applicableOnEntity: entry[1], type: type).save()
                    new RolePermission(role: role, permission: permission, isAllowed: true).save()
                }
            }
        }
        initSnippetTemplates()
    }


    def removeWidget(){
        def widgetList =  Widget.createCriteria().list {
            eq("widgetType", SNIPPET)
        }
        widgetList.each { widget ->
            def layoutObj = Layout.get(widget.containerId)
            if (widget && layoutObj) {
                layoutObj.removeFromHeaderWidgets(widget)
                layoutObj.removeFromFooterWidgets(widget)
            }else if (widget) {
                DockSection dockSection = DockSection.createCriteria().get {
                    widgets {
                        eq ("uuid", widget.uuid)
                    }
                }
                if (dockSection) {
                    dockSection.removeFromWidgets(widget)
                    dockSection.save()
                }
            }
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removePermission(SNIPPET)
            util.removePermission("snippet_template")
            util.removeFoldersFromModifiableResource(SnippetResourceTagLib.SNIPPET_TEMPLATES)
            SnippetResourceTagLib.RESOURCES_PATH.each { resource ->
                util.deleteResourceFolders(resource.value)
            }
            util.removeWidget(SNIPPET)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin blog From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin(WS)
        Holders.grailsApplication.mainContext.getBean(TemplateDataProviderService).metaClass.mixin(TDPS)
        Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).metaClass.mixin(TIS)
        Holders.grailsApplication.mainContext.getBean(DisposableUtilService).putDisposableUtilFactory(SNIPPET, snippetService)
        Holders.grailsApplication.mainContext.getBean(AppResourceTagLib).metaClass.mixin SnippetResourceTagLib

        AppEventManager.on("before-snippet-delete", { id ->
            WidgetContent.createCriteria().list {
                eq("type", SNIPPET)
                eq("contentId", id)
            }.each {
                it.delete()
            }
        })

        HookManager.register("content-explorer-view-model", { Map model ->
            SnippetService snippetService = Holders.grailsApplication.mainContext.getBean(SnippetService)
            Integer count = snippetService.getSnippetCount(model.params) + model.count
            List<Snippet> snippetList
            if(model.max == -1 || model.contents.article.size() < model.max ) {
                Integer max = model.max - model.contents.article.size()
                Integer offset = model.offset - model.count
                snippetList = snippetService.getSnippetListForExplorer(model.params, [max: max, offset: offset])
            }
            model.contents["snippet"] = snippetList
            model.count = count
            return model
        })

        TenantContext.eachParallelWithWait(tenantInit)

        AppEventManager.on("snippet-update", { id ->
            TemplateContent.where {
                contentType == SNIPPET
                contentId == id
            }.deleteAll()
        })

        HookManager.register("render-snippet-widget", {Map result, Widget widget, Writer writer, Closure widgetRenderer ->
            Long id = widget.widgetContent.size() ? widget.widgetContent[0].contentId : 0
            Snippet snippet = Snippet.get(id)
            if(snippet && result.updated < snippet.updated) {
                result.updated = snippet.updated
            }
            widgetService.renderSnippetWidget(widget, writer)
        })
    }

    static void initSnippetTemplates() {
        String systemRepositoryPath = SnippetResourceTagLib.getWebInfSnippetSystemResource()
        String localRepositoryPath = SnippetResourceTagLib.getModifiableResourceWebInfAbsolutePath()
        File systemRepository = new File(systemRepositoryPath)

        if(systemRepository.exists()) {
            systemRepository.eachFile(FileType.DIRECTORIES, { File template ->
                File local = new File(localRepositoryPath, template.name)
                File system = new File(systemRepository, template.name)
                local.mkdirs()
                system.eachFile { File file ->
                    File copyFile = new File(localRepositoryPath, template.name + "/" + file.name);
                    if (copyFile.exists()) {
                        copyFile.delete()
                    }
                    copyFile.createNewFile()
                    file.withInputStream { stream ->
                        copyFile << stream
                    }
                }
            })
        }


    }



}

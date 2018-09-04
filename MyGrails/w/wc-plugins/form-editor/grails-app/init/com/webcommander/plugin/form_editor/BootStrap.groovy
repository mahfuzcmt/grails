package com.webcommander.plugin.form_editor

import com.webcommander.Page
import com.webcommander.admin.DisposableUtilService
import com.webcommander.admin.MessageSource
import com.webcommander.admin.TrashService
import com.webcommander.config.EmailTemplate
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.Layout
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.plugin.form_editor.admin.FormService
import com.webcommander.plugin.form_editor.mixin_service.WidgetService as ECWS
import com.webcommander.plugin.form_editor.mixin_service.installation.template.TemplateDataProviderService as TDPS
import com.webcommander.plugin.form_editor.mixin_service.installation.template.TemplateInstallationService as TIS
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.util.Holders

class BootStrap {

    private final String FORM = "form"


    List domain_constants = [
            [constant:"WIDGET_CONTENT_TYPE", key: "FORM", value: FORM],
            [constant:"WIDGET_TYPE", key: "FORM", value: FORM]
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: FORM + ".title", value:"form.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: FORM + ".label", value:FORM],
            [constant:"WIDGET_LICENSE", key: FORM, value:"allow_form_builder_feature"]
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
        if(!MessageSource.findByMessageKeyAndLocale("submit", "all")) {
            new MessageSource(messageKey: "submit", message: "Submit", locale: "all").save();
        }
        if (!EmailTemplate.findByIdentifier("form-submit-to-email")) {
            Map emailTemplate = [
                    label     : "form.submission",
                    identifier: "form-submit-to-email",
                    subject   : "Submitted Data for the form '%form_name%'",
                    type      : DomainConstants.EMAIL_TYPE.CUSTOMER
            ];
            new EmailTemplate(emailTemplate).save()
        }
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            util.removeEmailTemplates("form-submit-to-email")
            util.removeSiteMessage("submit")
            util.removeWidget(FORM)
            NamedConstants.removeConstant(named_constants)
            DomainConstants.removeConstant(domain_constants)
        } catch(Exception e) {
            log.error "Could Not Deactivate Plugin form-editor From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(TemplateDataProviderService).metaClass.mixin TDPS
        Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).metaClass.mixin TIS
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin ECWS
        Holders.grailsApplication.mainContext.getBean(DisposableUtilService).
                putDisposableUtilFactory(FORM, Holders.grailsApplication.mainContext.getBean(FormService))
        TrashService.domains.put("Form", "formService")
        HookManager.register("available-widgets") { widgets ->
            if (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_form_builder_feature")) {
                def modified = new ArrayList<String>(widgets)
                modified.removeAll { it == "form" }
                modified
            }
        }
        HookManager.register("form-put-trash-at2-count") { response, id ->
            def pages = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", FORM)
                    eq("contentId", id)
                }
            }
            if (pages.size()) {
                int pageCount = pages.count { it[1] == "page" }
                int layoutCount = pages.count { it[1] == "layout" }
                if (pageCount) {
                    response.page = pageCount
                }
                if (layoutCount) {
                    response.layout = layoutCount
                }
            }
            return response;
        }
        HookManager.register("form-put-trash-at2-list") { response, id ->
            def widgets = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", FORM)
                    eq("contentId", id)
                }
            }
            if (widgets.size()) {
                int pageCount = widgets.count { it[1] == "page" }
                int layoutCount = widgets.count { it[1] == "layout" }
                if (pageCount) {
                    List pages = Page.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults { it[1] == "page" ? it[0] : null })
                    }
                    response.page = pages
                }
                if (layoutCount) {
                    List layouts = Layout.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults { it[1] == "layout" ? it[0] : null })
                    }
                    response.layout = layouts
                }
            }
            return response;
        }
        AppEventManager.on("before-form-put-in-trash", { id ->
            WidgetContent.createCriteria().list {
                eq("type", FORM)
                eq("contentId", id)
            }.each {
                it.delete()
            }
        })
        AppEventManager.on("form-update", { id ->
            TemplateContent.where {
                contentType == FORM
                contentId == id
            }.deleteAll()
        });
        TenantContext.eachParallelWithWait(tenantInit)
    }
}

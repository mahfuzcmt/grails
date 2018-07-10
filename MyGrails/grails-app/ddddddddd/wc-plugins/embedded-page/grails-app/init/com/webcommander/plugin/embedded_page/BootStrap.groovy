package com.webcommander.plugin.embedded_page

import com.webcommander.Page
import com.webcommander.admin.DisposableUtilService
import com.webcommander.constants.DomainConstants
import com.webcommander.design.Layout
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.InstallationDataHolder
import com.webcommander.installation.template.TemplateContent
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.HookManager
import com.webcommander.models.TemplateData
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.widget.Widget
import grails.util.Holders
import com.webcommander.plugin.embedded_page.mixin_services.TemplateDataProviderService as TDPS
import com.webcommander.plugin.embedded_page.mixin_services.TemplateInstallationService as TIS
import groovy.util.logging.Log

import java.util.logging.Level

@Log
class BootStrap {

    List domain_constants = [
            [constant:"WIDGET_CONTENT_TYPE", key: "EMBEDDED_PAGE", value: "embeddedPage"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        try {
            DomainConstants.removeConstant(domain_constants)
        } catch (Exception e) {
            log.log Level.SEVERE, "Could Not Deactivate Plugin embedded-page From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(TemplateDataProviderService).metaClass.mixin TDPS
        Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).metaClass.mixin TIS

        TenantContext.eachParallelWithWait(tenantInit)

        Holders.grailsApplication.mainContext.getBean(DisposableUtilService).
                putDisposableUtilFactory(DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE, Holders.grailsApplication.mainContext.getBean(EmbeddedPageService))
        HookManager.register "save-page-container-type", { containerClass, containerType ->
            if(containerType == "embedded") {
                return EmbeddedPage
            }
        }

        HookManager.register "ecommerce-embedded-page-name", { containerId ->
            println("Container Id: " + containerId)
            return EmbeddedPage.findById(containerId).name
        }

        HookManager.register "embedcss-for-editor", { cssWriter, request ->
            if(request.embedded_page) {
                def appendCss = { def elm ->
                    cssWriter << "<style id='style-store-";
                    cssWriter << elm.uuid;
                    cssWriter << "'>";
                    cssWriter << elm.css;
                    cssWriter << "</style>";
                }
                EmbeddedPage page = EmbeddedPage.get(request.embedded_page);
                cssWriter << "<style id='stored-css'>";
                cssWriter << page.css;
                cssWriter << "</style>";
                Widget.createCriteria().list {
                    eq("containerId", request.embedded_page)
                    eq("containerType", "embedded")
                }.each {
                    appendCss.call(it)
                }
            }
            return cssWriter
        }

        HookManager.register("embedded-page-delete-at2-count") { response, id ->
            def pages = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE)
                    eq("contentId", id)
                }
            }
            if(pages.size()) {
                int pageCount = pages.count {it[1] == "page"}
                int layoutCount = pages.count {it[1] == "layout"}
                if(pageCount) {
                    response.page = pageCount
                }
                if(layoutCount) {
                    response.layout = layoutCount
                }
            }
            return response;
        }

        HookManager.register("embedded-page-delete-at2-list") { response, id ->
            def widgets = Widget.createCriteria().list {
                projections {
                    distinct(["containerId", "containerType"])
                }
                widgetContent {
                    eq("type", DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE)
                    eq("contentId", id)
                }
            }
            if(widgets.size()) {
                int pageCount = widgets.count {it[1] == "page"}
                int layoutCount = widgets.count {it[1] == "layout"}
                if(pageCount) {
                    List pages = Page.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "page" ? it[0] : null})
                    }
                    response.page = pages
                }
                if(layoutCount) {
                    List layouts = Layout.createCriteria().list {
                        projections {
                            property("name")
                        }
                        inList("id", widgets.findResults {it[1] == "layout" ? it[0] : null})
                    }
                    response.layout = layouts
                }
            }
            return response;
        }

        AppEventManager.on("copy-template-data", {TemplateData templateData, InstallationDataHolder installationDataHolder ->
            Holders.grailsApplication.mainContext.getBean(TemplateInstallationService).saveEmbeddedPageWidgets(templateData, installationDataHolder)
        });

        AppEventManager.on("embedded-update", { id ->
            TemplateContent.where {
                contentType == DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE
                contentId == id
            }.deleteAll()
        });
    }

    def destroy = {
    }
}
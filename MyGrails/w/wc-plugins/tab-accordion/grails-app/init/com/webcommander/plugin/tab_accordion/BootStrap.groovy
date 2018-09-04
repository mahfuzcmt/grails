package com.webcommander.plugin.tab_accordion

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.plugin.tab_accordion.controllers.admin.design.WidgetController
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.util.widget.WidgetDropper
import com.webcommander.plugin.tab_accordion.util.WidgetDropper as WD
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import com.webcommander.plugin.tab_accordion.mixin_service.WidgetService as TAWS
import grails.util.Holders
import com.webcommander.controllers.admin.design.WidgetController as WC

class BootStrap {

    private final String TAB_ACCORDIAN = "tabAccordion"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "TAB_ACCORDIAN", value: TAB_ACCORDIAN],
            [constant:"WIDGET_CONTENT_TYPE", key: "TAB_ACCORDIAN", value: TAB_ACCORDIAN],
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: TAB_ACCORDIAN + ".title", value:"tab.accordion.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: TAB_ACCORDIAN + ".label", value:"tab.accordion"],
            [constant:"WIDGET_LICENSE", key: TAB_ACCORDIAN, value:"allow_tab_accordion_feature"],
    ]

    def tenantInit = { tenant ->
        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        util.removeWidget(TAB_ACCORDIAN)
        DomainConstants.removeConstant(domain_constants)
        NamedConstants.removeConstant(named_constants)
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin TAWS
        WidgetDropper.mixin(WD)

        TenantContext.eachParallelWithWait(tenantInit)

        HookManager.register "tabAccordion-allcss", { hookCss, uuid, request ->
            EmbeddedPage.where {
                def ep = EmbeddedPage
                exists WidgetContent.where {
                    def content = WidgetContent
                    content.contentId == ep.id
                    widget {
                        uuid == uuid
                    }
                }.id()
            }.property("css").property("id").list().each { widget ->
                if (widget[0]) {
                    hookCss << widget[0] ?: ""
                    hookCss << "\n"
                }
                Long id = widget[1]
                Widget.createCriteria().list {
                    projections {
                        property("css")
                        property("uuid")
                        property("widgetType")
                    }
                    eq("containerType", "embedded")
                    eq("containerId", id)
                }.each {
                    if (it[0]) {
                        hookCss << it[0] ?: ""
                        hookCss << "\n"
                    }
                    HookManager.hook(it[2] + "-allcss", [hookCss, it[1], request])
                }
            }
            return hookCss
        }

        HookManager.register "tabAccordion-alljs", { hookJs, uuid, request ->
            EmbeddedPage.where {
                def ep = EmbeddedPage
                exists WidgetContent.where {
                    def content = WidgetContent
                    content.contentId == ep.id
                    widget {
                        uuid == uuid
                    }
                }.id()
            }.property("js").property("id").list().each { widget ->
                if (widget[0]) {
                    hookJs << "\ntry {\n"
                    hookJs << "(function() {\n"
                    hookJs << widget[0] ?: ""
                    hookJs << "\n})()\n"
                    hookJs << "\n} catch(e) {}\n"
                }
                Long id = widget[1]
                Widget.createCriteria().list {
                    projections {
                        property("js")
                        property("uuid")
                        property("widgetType")
                    }
                    eq("containerType", "embedded")
                    eq("containerId", id)
                }.each {
                    if (it[0]) {
                        hookJs << "\ntry {\n"
                        hookJs << "(function(widget) {\n"
                        hookJs << it[0] ?: ""
                        hookJs << "\n})(\$('#wi-${it[1]}'))\n"
                        hookJs << "\n} catch(e) {}\n"
                    }
                    HookManager.hook(it[2] + "-alljs", [hookJs, it[1], request])
                }
            }
            return hookJs
        }

        AppEventManager.on "before-embedded-page-delete", { id, at1_reply ->
            WidgetContent.createCriteria().list {
                eq "contentId", id
                eq "type", DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE
            }*.delete()
        }
    }

    def destroy = {
    }
}

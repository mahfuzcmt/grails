package com.webcommander.plugin.section_slider


import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.design.WidgetService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.util.Holders
import com.webcommander.plugin.section_slider.mixin_service.WidgetService as SSWS


class BootStrap {

    private final String SECTION_SLIDER = "sectionSlider"

    List domain_constants = [
            [constant:"WIDGET_TYPE", key: "SECTION_SLIDER", value: SECTION_SLIDER],
    ]

    List named_constants = [
            [constant:"WIDGET_MESSAGE_KEYS", key: "sectionSlider.title", value:"section.slider.widget"],
            [constant:"WIDGET_MESSAGE_KEYS", key: "sectionSlider.label", value:"section.slider"],
            [constant:"WIDGET_LICENSE", key: SECTION_SLIDER, value:"allow_section_slider_feature"],
    ]

    def tenantInit = { tenant ->

        DomainConstants.addConstant(domain_constants)
        NamedConstants.addConstant(named_constants)
    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil util = new PluginDestroyUtil()
        util.removeWidget(SECTION_SLIDER)
        DomainConstants.removeConstant(domain_constants)
        NamedConstants.removeConstant(named_constants)
    }

    def init = { servletContext ->
        Holders.grailsApplication.mainContext.getBean(WidgetService).metaClass.mixin SSWS
        TenantContext.eachParallelWithWait(tenantInit)
        HookManager.register "sectionSlider-allcss", { hookCss, uuid, request ->
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
                if(widget[0]) {
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
                    if(it[0]) {
                        hookCss << it[0] ?: ""
                        hookCss << "\n"
                    }
                    HookManager.hook(it[2] + "-allcss", [hookCss, it[1], request])
                }
            }
            return hookCss
        }

        HookManager.register "sectionSlider-alljs", { hookJs, uuid, request ->
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
                if(widget[0]) {
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
                    if(it[0]) {
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
}
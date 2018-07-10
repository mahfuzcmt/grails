package com.webcommander.plugin.tab_accordion.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap

class WidgetService {
    def populateTabAccordionInitialContentNConfig(Widget widget) {
        List<EmbeddedPage> pages = EmbeddedPage.createCriteria().list {
            eq("isDisposable", false)
            maxResults 3
        }
        LinkedList widgetContents = [];
        pages.each {
            widgetContents.push(new WidgetContent(widget: widget, contentId: it.id, type: DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE, extraProperties: it.name));
        }
        widget.widgetContent = widgetContents
        widget.params = ([type: "tab", axis: "h"] as JSON).toString()
    }

    def renderTabAccordionWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params);
        List<WidgetContent> contents = widget.widgetContent ? widget.widgetContent as List : []
        List pages = []
        widget.widgetContent.each { section ->
            pages.add(EmbeddedPage.get(section.contentId))
        }
        renderService.renderView("/plugins/tab_accordion/widget/tabAccordionWidget", [widget: widget, config: config, contents: contents, pages: pages], writer)
    }

    def saveTabAccordionWidget(Widget widget, GrailsParameterMap params) {
        Map paramsMap = [type: params.type, axis: params.axis]
        params.remove("tempSelect");
        LinkedList contents = []
        LinkedList widgetContents = []
        List contentName = params.list("contentName")
        List contentId = params.list("contentId")
        contentId.eachWithIndex { id, i ->
            widgetContents.push(new WidgetContent(widget: widget, contentId: contentId[i], type: DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE, extraProperties:  contentName[i]));
        }
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString();
        widget.widgetContent = widgetContents
    }
}

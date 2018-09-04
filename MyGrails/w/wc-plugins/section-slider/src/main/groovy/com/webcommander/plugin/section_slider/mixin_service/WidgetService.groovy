package com.webcommander.plugin.section_slider.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.widget.Widget
import com.webcommander.widget.WidgetContent
import grails.converters.JSON

/**
 * Created by zobair on 23/10/2014.
 */
class WidgetService {
    def populateSectionSliderInitialContentNConfig(Widget widget) {
        widget.params = ([height: "800"] as JSON).toString()
        EmbeddedPage page = EmbeddedPage.findByIsDisposable(false)
        if(page) {
            widget.widgetContent.add(new WidgetContent(widget: widget, contentId: page.id, type: DomainConstants.WIDGET_CONTENT_TYPE.EMBEDDED_PAGE))
        }
    }

    def renderSectionSliderWidget(Widget widget, Writer writer) {
        def config = [:];
        if (widget.params) {
            config = JSON.parse(widget.params)
        }
        List pages = []
        widget.widgetContent.each { section ->
            pages.add(EmbeddedPage.get(section.contentId))
        }
        renderService.renderView("/plugins/section_slider/renderWidget", [widget: widget, config: config, pages: pages], writer)
    }
}

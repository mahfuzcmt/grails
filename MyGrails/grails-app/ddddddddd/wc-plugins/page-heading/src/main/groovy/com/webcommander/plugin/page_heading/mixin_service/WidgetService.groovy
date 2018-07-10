package com.webcommander.plugin.page_heading.mixin_service

import com.webcommander.widget.Widget

class WidgetService {
    def populatePageHeadingInitialContentNConfig(Widget widget) {
    }

    def renderPageHeadingWidget(Widget widget, Writer writer) {
        renderService.renderView("/plugins/page_heading/widget/pageTitleWidget", [widget: widget], writer)
    }
}
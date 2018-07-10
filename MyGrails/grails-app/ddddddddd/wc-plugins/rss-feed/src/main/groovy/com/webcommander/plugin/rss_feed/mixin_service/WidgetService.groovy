package com.webcommander.plugin.rss_feed.mixin_service

import com.webcommander.widget.Widget
import grails.converters.JSON

class WidgetService {

    def populateRssFeedInitialContentNConfig(Widget widget) {
        widget.params = ([
            item_to_display: "10",
            show_title: "true",
            show_content: "true",
            show_author: "false",
            show_date: "true"] as JSON).toString()
        widget.content = "https://www.yahoo.com/news/rss/entertainment"
    }

    def renderRssFeedWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        def url = widget.content
        try {
            def feed = new XmlSlurper().parse(url)
            renderService.renderView("/plugins/rss_feed/widget/rssFeedWidget", [widget: widget, config: config, feed: feed], writer)
        } catch (Exception e) {}

    }
}

package com.webcommander.plugin.rss_feed.controllers.admin.design


class WidgetController {


    def rssFeedShortConfig() {
        render(view: "/plugins/rss_feed/admin/loadRssFeedWidgetShort", model: [noAdvance: true]);
    }
}
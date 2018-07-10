package com.webcommander.plugin.news.controllers.admin.design


class WidgetController {

    def widgetService

    def newsShortConfig() {
        render(view: "/plugins/news/admin/widget/shortConfig/loadNews", model: [noAdvance: true])
    }

    def saveNewsWidget() {
        render(widgetService.saveAnyWidget("News", params))
    }

}
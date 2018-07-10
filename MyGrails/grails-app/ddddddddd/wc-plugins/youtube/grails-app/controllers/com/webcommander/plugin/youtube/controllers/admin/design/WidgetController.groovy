package com.webcommander.plugin.youtube.controllers.admin.design


/**
 * Created by zobair on 22/10/13.
 */
class WidgetController {

    def widgetService

    def editYoutube() {
        render(view:  "/plugins/youtube/admin/loadYoutube", model:  [:])
    }


    def youtubeShortConfig() {
        render(view:  "/plugins/youtube/admin/loadYoutubeShort", model:  [advanceText: g.message(code: "configure")])
    }

    def saveYoutubeWidget() {
        render(widgetService.saveAnyWidget("Youtube", params))
    }
}

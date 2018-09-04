package com.webcommander.plugin.youtube.mixin_service

import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap

class WidgetService {

    def populateYoutubeInitialContentNConfig(Widget widget) {
        widget.params = ([
            width: 350,
            height: 400,
            allowFullScreen: 'no',
            autoPlay: '0',
            showSuggesion: '0',
            showControls: '0',
            showInfo: '0',
        ] as JSON).toString()
        widget.content = "AhgtoQIfuQ4"
    }

    def renderYoutubeWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/plugins/youtube/widget/youtubeWidget", [widget: widget, config: config], writer)
    }

    def saveYoutubeWidget(Widget widget, GrailsParameterMap params) {
        Map paramsMap = [
            showTitle: params.showTitle,
            width: params.width,
            height: params.height,
            allowFullScreen: params.allowFullScreen,
            autoPlay: params.autoPlay,
            showSuggesion: params.showSuggesion,
            showControls: params.showControls,
            showInfo: params.showInfo,
        ];
        widget.content = params.mediaUrl
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString();
    }
}

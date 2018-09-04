package com.webcommander.plugin.google_map.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.converters.JSON

class WidgetService {

    def populateGoogleMapInitialContentNConfig(Widget widget) {
        widget.params = ([
            longitude: '144.966667',
            latitude: '-37.816667',
            zoom: 16,
            api_key: AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_MAP, "api_key")
        ] as JSON).toString()
    }

    def renderGoogleMapWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/plugins/google_map/widget/googleMapWidget", [widget: widget, config: config], writer)
    }

    def saveGoogleMapWidget(Widget widget, Map params) {
        Map paramMap = [
            longitude: params.longitude,
            latitude: params.latitude,
            zoom: params.zoom,
            radius: params.radius,
            popup_text: params.popup_text,
            pin_url: params.pin_url,
            api_key: params.api_key
        ]
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }
}

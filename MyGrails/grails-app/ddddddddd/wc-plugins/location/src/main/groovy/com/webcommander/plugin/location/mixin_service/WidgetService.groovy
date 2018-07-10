package com.webcommander.plugin.location.mixin_service

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.widget.Widget
import grails.converters.JSON

class WidgetService {

    def populateLocationInitialContentNConfig(Widget widget) {
        widget.params = ([
                longitude: '-25.2744',
                latitude: '133.7751',
                zoom: 4,
                api_key: AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCATION_WIDGET_DEFAULT_ADDRESS, "api_key")

        ] as JSON).toString()
    }

    def renderLocationWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/plugins/location/widget/locationWidget", [widget: widget, config: config], writer)
    }

    def saveLocationWidget(Widget widget, Map params) {
        Map paramMap = [
                longitude: params.longitude,
                latitude: params.latitude,
                zoom: params.zoom,
                pin_url: params.pin_url,
                api_key: params.api_key
        ]
        widget.params = JSON.use("deep") {
            paramMap as JSON
        }.toString()
    }
}

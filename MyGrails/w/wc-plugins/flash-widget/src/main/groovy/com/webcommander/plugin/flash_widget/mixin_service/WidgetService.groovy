package com.webcommander.plugin.flash_widget.mixin_service

import com.webcommander.events.AppEventManager
import com.webcommander.plugin.flash_widget.util.WidgetDropper
import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.annotation.Transactional

class WidgetService {

    static {
        AppEventManager.on("flash-widget-after-drop") { widget ->
            WidgetDropper.afterDropWidget(widget.uuid);
        }
    }

    def populateFlashInitialContentNConfig(Widget widget) {
        widget.params = ([
            width: 400,
            height: 300,
            upload_type: "direct",
            paramName: ["play", "loop", "menu", "scale", "wmode", "swliveconnect", "devicefont", "allowscriptaccess", "seamlesstabbing", "allowfullscreen", "allownetworking"],
            paramValue: ["true", "true", "true", "showall", "opaque", "false", "false", "samedomain", "true", "true", "all"]
        ] as JSON).toString();
        widget.content = "http://gameswf1.weebly.com/uploads/2/0/0/6/20065369/______v8musclecars.swf"
    }

    def renderFlashWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        renderService.renderView("/plugins/flash_widget/widget/flashWidget", [widget: widget, config: config], writer)
    }

    @Transactional(readOnly = true)
    def saveFlashWidget(Widget widget, GrailsParameterMap params) {
        Map paramsMap = [
            width: params.width ?: 400,
            height: params.height ?: 300,
            upload_type: params.upload_type,
            paramName: params.paramName ? params.list("paramName") : [],
            paramValue: params.paramValue ? params.list("paramValue") : [],
            attributes: params.attributes
        ];
        widget.content = params[params.upload_type + "_url"]
        widget.params = JSON.use("deep") {
            paramsMap as JSON
        }.toString();
    }
}

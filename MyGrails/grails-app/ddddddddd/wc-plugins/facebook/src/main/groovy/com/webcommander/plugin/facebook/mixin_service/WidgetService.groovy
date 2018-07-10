package com.webcommander.plugin.facebook.mixin_service

import com.webcommander.widget.Widget
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by sanjoy on 6/18/2014.
 */
class WidgetService {

    def populateFacebookInitialContentNConfig(Widget widget) {
        widget.params = ([
            tab_enabled: "true",
            share_enabled: "false",
            invite_enabled: "false"] as JSON).toString();
    }

    def renderFacebookWidget(Widget widget, Writer writer){
        def config = JSON.parse(widget.params)
        renderService.renderView("/plugins/facebook/widget/facebookWidget", [widget: widget, config: config], writer)
    }

    def saveFacebookWidget(Widget widget, GrailsParameterMap params){
        Map parameterMap = [
            title: params.title,
            tab_enabled: params.tab_enabled,
            share_enabled: params.share_enabled,
            invite_enabled: params.invite_enabled,
            invite_message: params.invite_message
        ]
        return widget.params = JSON.use("deep"){
            parameterMap as JSON
        }.toString();
    }
}

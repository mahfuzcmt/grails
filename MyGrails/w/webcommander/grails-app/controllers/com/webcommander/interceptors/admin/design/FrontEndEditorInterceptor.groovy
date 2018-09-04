package com.webcommander.interceptors.admin.design

import com.webcommander.design.WidgetService
import grails.converters.JSON

/**
 * interceptor for associated controller
 */
class FrontEndEditorInterceptor {
    WidgetService widgetService

    boolean before() {
        if (!params.widgetId) {
            params.widgetId = 0
        }
        if (params.action.endsWith("Config")) {
            Map obj = widgetService.initWidget(params)
            params.widget = obj.widget
            params.config = obj.config
        }
        return true
    }

    boolean after() {
        if (params.action?.endsWith("Config")) {
            model.widget = params.widget
            model.config = params.config
            model.widgetParams = [:]
            if (params.widget && params?.widget?.params) {
                model.widgetParams = JSON.parse(params?.widget?.params)
            }
        }
        return true
    }
}

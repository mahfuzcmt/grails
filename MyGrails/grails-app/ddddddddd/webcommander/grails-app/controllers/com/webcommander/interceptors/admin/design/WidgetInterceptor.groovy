package com.webcommander.interceptors.admin.design

import com.webcommander.design.WidgetService
import grails.converters.JSON

/**
 * interceptor for associated controller
 */
class WidgetInterceptor {
    WidgetService widgetService

    boolean before() {
        if (params.action.startsWith("edit") || params.action == "renderWidget" || params.action.endsWith("ShortConfig")) {
            Map obj = widgetService.initWidget(params)
            params.widget = obj.widget
            params.config = obj.config
        }

        if (params.action.startsWith("save") && params['params-cache']) {
            Map paramsCache = JSON.parse(params['params-cache'])
            params.lmerge(paramsCache)
            if (params.params) {
                Map _params = JSON.parse(params.params)
                _params.lmerge(paramsCache)
                params.params = (_params as JSON).toString()
            }
        }
        return true
    }

    boolean after() {
        if (params.action.startsWith("edit") || params.action.endsWith("ShortConfig")) {
            model.widget = params.widget
            model.config = params.config
            // FOR frontEnd Editor
            if (params.fetchSerialize && !params.fetchHtml) {
                render([serialized: model.widget?.serialize(), htmlContent: ''] as JSON)
            } else if (params.fetchSerialize && params.fetchHtml) {
                params.noLayout = true
                def htmlContent = widgetService.renderWidget(model.widget.widgetType?.camelCase(), model.widget)
                render([serialized: model.widget?.serialize(), htmlContent: htmlContent] as JSON)
            }
        }
        return true
    }
}

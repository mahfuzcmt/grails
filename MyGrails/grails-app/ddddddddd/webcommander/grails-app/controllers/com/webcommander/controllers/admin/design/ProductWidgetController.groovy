package com.webcommander.controllers.admin.design

import com.webcommander.design.ProductWidgetService
import grails.converters.JSON
import org.grails.buffer.FastStringWriter

class ProductWidgetController {
    ProductWidgetService productWidgetService

    def renderWidget() {
        Writer writer = new FastStringWriter();
        request.editMode = true;
        def widgetContent = productWidgetService."render${params.type.capitalize()}WidgetForEditor"([:], writer)
        if (writer.toString()) {
            render([status: "success", message: g.message(code: "widget.save.successfully"), html: widgetContent] as JSON);
        } else {
            render([status: "error", message: g.message(code: "widget.save.failure")] as JSON);
        }
    }

    def save () {
        if(productWidgetService.saveContent(params)) {
            render([status: "success", message: g.message(code: "content.successful.update"), containerId: params.containerId] as JSON);
        } else {
            render([status: "error", message: g.message(code: "content.failure.update")] as JSON);
        }
    }
}

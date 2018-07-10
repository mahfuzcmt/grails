package com.webcommander

import com.webcommander.design.ProductWidgetService
import com.webcommander.design.WidgetService
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.widget.Widget
import org.hibernate.SessionFactory

class WidgetTagLib {

    static namespace = "wi"

    WidgetService widgetService
    SessionFactory sessionFactory
    RenderService renderService
    ProductWidgetService productWidgetService
    
    def widget = { attrs, body ->
        Widget widget
        try {
            widget = attrs.widget
            String widgetType = attrs.type
            if(attrs["uuid"]) {
                widget = widgetService.getWidget(attrs["uuid"])
            }
            if(widget || widgetType == "page") {
                widgetService."render${attrs.type.capitalize()}Widget"(widget, out)
            }
        } catch(UnconfiguredWidgetExceptions t) {
            if(request.editMode) {
                attrs.widget = widget
                renderService.renderView("/widget/unconfiguredWidget", attrs, out)
            }
        } catch(MissingMethodException t) {
            if(t.method.startsWith("render")) {
                if(request.editMode) {
                    attrs.widget = widget
                    renderService.renderView("/widget/unconfiguredWidget", attrs, out)
                }
            } else {
                log.error "Error Occurred in rendering widget : " + (attrs.widget ? attrs.widget.uuid : attrs.uuid), t
            }
        } catch(Throwable t) {
            if(request.editMode) {
                attrs.widget = widget
                renderService.renderView("/widget/malfunctionedWidget", attrs, out)
            }
            log.error("Error Occurred in rendering widget : " + (attrs.widget ? attrs.widget.uuid : attrs.uuid) + " Cause: ${t.message}")
        }
        sessionFactory.currentSession.discard()
    }

    def productwidget = { attrs, body ->
        try {
            request.widgetType = attrs.type
            request.clazz = attrs["class"]
            attrs << [product: request.product ?: attrs.product]
            attrs << [productData: request.productData ?: attrs.productData]
            productWidgetService."render${attrs.type.capitalize()}Widget${request.editMode ? "ForEditor" : ""}"(attrs, out)
        } catch(MissingMethodException t) {
            if(t.method.startsWith("render")) {
            } else {
                log.error "Error Occurred in rendering widget : " + attrs.type.capitalize(), t
            }
        } catch(Throwable t) {
            log.error "Error Occurred in rendering widget : " + attrs.type.capitalize(), t
        }
        sessionFactory.currentSession.discard()
    }
}

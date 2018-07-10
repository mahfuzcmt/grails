package com.webcommander.plugin.swipe_box_slider.controllers.site.webcontent

import com.webcommander.design.GalleryWidgetService
import com.webcommander.widget.Widget
import grails.converters.JSON

class SwipeBoxSliderController {
    GalleryWidgetService galleryWidgetService

    def lazyLoad() {
        Widget widget = Widget.get(params.id);
        Map config = widget.params ? JSON.parse(widget.params) : [:]
        Map model = [widget: widget]
        Integer max = config.max ? config.max.toInteger() : -1
        Integer offset = params.offset.toInteger(0)
        offset = offset + max
        params.offset = ""+offset
        if(widget.widgetContent.size()) {
            model = galleryWidgetService.getGalleryWidgetMap(model)
        }
        def html = g.include(view: "/plugins/swipe_box_slider/swipeBox.gsp", model: model)
        Boolean end = false
        if((model.totalCount - offset) <= params.max) {
            end = true
        }
        render([status: "success", html: html, offset: offset, end: end] as JSON)
    }
}

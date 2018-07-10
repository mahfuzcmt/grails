package com.webcommander.plugin.jssor_slider

import com.webcommander.content.AlbumImage

class JssorSliderShareTagLib {
    static namespace = "jssorSlider"
    public static final String BLOG = "blog"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/jssor-slider/js/shared/jssor-slider-widget.js')
    }

    def albumTabHeader = { attr, body ->
        out << body()
        out << '<div class="bmui-tab-header" data-tabify-tab-id="jssor-slider">'
        out << '<span class="title">' + g.message(code: "jssor.slider") + '</span>'
        out << '</div>'
    }

    def albumTabBody = { attrs, body ->
        out << body()
        AlbumImage image = attrs.albumImage
        out << g.include(view: "plugins/jssor_slider/admin/imageConfig.gsp", model: [imageId: image.id])
    }
}
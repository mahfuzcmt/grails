package com.webcommander.plugin.anything_slider

class AnythingSliderShareTagLib {
    static namespace = "anythingSlider"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/anything-slider/js/shared/anything-slider-widget.js')
    }
}
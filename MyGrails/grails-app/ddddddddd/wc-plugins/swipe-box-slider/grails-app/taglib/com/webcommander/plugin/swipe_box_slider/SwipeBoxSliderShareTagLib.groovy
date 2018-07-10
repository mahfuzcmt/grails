package com.webcommander.plugin.swipe_box_slider

class SwipeBoxSliderShareTagLib {
    static namespace = "swipeSlider"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/swipe-box-slider/js/shared/swipe-slider-widget.js')
    }
}
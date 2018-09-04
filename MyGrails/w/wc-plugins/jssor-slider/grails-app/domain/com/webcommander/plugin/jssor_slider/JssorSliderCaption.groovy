package com.webcommander.plugin.jssor_slider

import com.webcommander.content.AlbumImage

class JssorSliderCaption {

    Long id
    String type //title, description, button
    String text
    String url
    String animation
    Long duration = 0
    Long delay = 0

    AlbumImage image

    static constraints = {
        url(maxSize: 1000, nullable: true)
        text(nullable: true)
        animation(nullable: true)
    }

    static mapping = {
        text(type: "text")
    }

}

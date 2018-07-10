package com.webcommander.plugin.gallery_owl_carousel

class GalleryOwlCarouselShareTagLib {
    static namespace = "owlCarousel"
    public static final String BLOG = "blog"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/gallery-owl-carousel/js/shared/owl-carousel-widget.js')
    }
}

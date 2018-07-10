package com.webcommander.plugin.gallery_flip_book

class GalleryFlipBookShareTagLib {
    static namespace = "flipBook"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/gallery-flip-book/js/shared/flip-book-widget.js')
    }
}

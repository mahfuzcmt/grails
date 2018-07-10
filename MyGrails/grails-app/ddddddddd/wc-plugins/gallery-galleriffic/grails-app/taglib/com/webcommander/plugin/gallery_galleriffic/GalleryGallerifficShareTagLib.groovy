package com.webcommander.plugin.gallery_galleriffic

class GalleryGallerifficShareTagLib {
    static namespace = "galleryGalleriffic"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/gallery-galleriffic/js/shared/galleriffic-widget.js')
    }
}

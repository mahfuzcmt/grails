package com.webcommander.plugin.google_product.controllers.site

import com.webcommander.plugin.google_product.GoogleProductService

class GoogleProductController {
    GoogleProductService googleProductService

    def feed() {
        response.setContentType("text/xml")
        response.setCharacterEncoding("UTF-8")
        Writer writer = new OutputStreamWriter(response.outputStream)
        googleProductService.writeXmlFeed(writer)
        writer.close()
        response.outputStream.flush()
    }
}

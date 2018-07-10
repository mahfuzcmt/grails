package com.webcommander.plugin.my_shopping.controllers.site

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.my_shopping.MyShoppingService

class MyShoppingController {
    MyShoppingService myShoppingService

    @License(required = "allow_myshopping_feature")
    def feed() {
        response.setContentType("text/xml")
        response.setCharacterEncoding("UTF-8")
        Writer writer = new OutputStreamWriter(response.outputStream)
        myShoppingService.writeXmlFeed(writer)
        writer.close()
    }
}

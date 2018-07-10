package com.webcommander.plugin.tab_accordion.controllers

import com.webcommander.plugin.embedded_page.EmbeddedPage
import com.webcommander.webcommerce.ProductService

class TabAccordionController {
    ProductService productService

    def loadContentSelection() {
        String type = params.contentType
        List children = EmbeddedPage.createCriteria().list {
            if(params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
        }
        render(view: "/plugins/tab_accordion/admin/widget/contentPopup", model: [children: children])
    }
}

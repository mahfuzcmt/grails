package com.webcommander.plugin.wish_list

class UrlMappings {
    static mappings = {
        "/wishlist/products/$id"(controller: "wishlist", action: "details")
    }
}

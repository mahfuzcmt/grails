package com.webcommander.plugin.gift_registry

class UrlMappings {
    static mappings = {
        "/gift-registry/products/$id"(controller: "giftRegistry", action: "details")
    }
}

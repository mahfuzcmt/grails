package com.webcommander.plugin.blog

class UrlMappings {
    static mappings = {
        "/blog-category/$url"(controller: "blogPage", action: "blogCategory")
        "/blog/$url"(controller: "blogPage", action: "post")
    }
}

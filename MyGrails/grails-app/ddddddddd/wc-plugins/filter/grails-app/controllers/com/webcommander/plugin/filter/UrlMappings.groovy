package com.webcommander.plugin.filter

/**
 * Created by sharif ul islam on 17/04/2018.
 */
class UrlMappings {

    static mappings = {
        "/filter/$url"(controller: "filterPage", action: "filterGroupItem")
    }

}

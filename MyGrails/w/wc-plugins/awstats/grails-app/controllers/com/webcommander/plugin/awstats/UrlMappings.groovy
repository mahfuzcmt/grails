package com.webcommander.plugin.awstats

/**
 * Created by sajedur on 18-12-2014.
 */
class UrlMappings {
    static mappings = {
        "/awstats/$file**.$ext" (controller: "awstats", action: "staticResources"){
            fileName = {
                params.file + "." + params.ext
            }
        }
    }
}


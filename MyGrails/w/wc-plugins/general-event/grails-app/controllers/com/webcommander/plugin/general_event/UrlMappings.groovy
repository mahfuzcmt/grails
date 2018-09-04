package com.webcommander.plugin.general_event

class UrlMappings {

    static mappings = {
        "/location/$url"(controller: "generalEvent", action: "venueLocation")
        "/generalEvent/$id"(controller: "generalEvent", action: "getEventDetails") {
            constraints {
                id matches: /\d+/
            }
        }
    }
}


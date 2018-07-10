package com.webcommander.plugin.simplified_event_management

class UrlMappings {
    static mappings = {
        "/simplifiedEvent/$id"(controller: "simplifiedEvent", action: "details") {
            constraints {
                id matches: /\d+/
            }
        }
    }
}


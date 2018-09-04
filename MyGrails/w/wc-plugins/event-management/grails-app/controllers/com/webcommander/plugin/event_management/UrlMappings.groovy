package com.webcommander.plugin.event_management

class UrlMappings {
    static excludes = ["/event/90", "get:/event/100"]

    static mappings = {
        "/venueLocation/$url"(controller: "event", action: "venueLocation")
        "/event/$id"(controller: "event", action: "details") {
            constraints {
                id matches: /\d+/
            }
        }
        "/event/$eventId/session/$sessionId"(controller: "event", action: "sessionDetails")
    }
}


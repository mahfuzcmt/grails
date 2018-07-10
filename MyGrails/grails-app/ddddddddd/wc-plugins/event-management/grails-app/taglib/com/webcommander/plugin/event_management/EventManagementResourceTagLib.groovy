package com.webcommander.plugin.event_management

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class EventManagementResourceTagLib {
    static namespace = "appResource"

    public static final RESOURCES_PATH = [
            "EVENT" : "event"
    ]

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    def getEventImageRelativeUrl(def eventId) {
        return "${RESOURCES_PATH.EVENT}/event-$eventid/images/"
    }
}

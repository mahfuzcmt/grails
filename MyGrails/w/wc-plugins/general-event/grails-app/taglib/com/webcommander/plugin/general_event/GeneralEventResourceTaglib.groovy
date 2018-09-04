package com.webcommander.plugin.general_event

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class GeneralEventResourceTaglib {

        static namespace = "appResource"

        public static final RESOURCES_PATH = [
                "GENERAL_EVENT" : "general-event",
                "GENERAL_EVENT_VENUE_LOCATION" : "general-event-venue-location"
        ]

        @Autowired
        @Qualifier("com.webcommander.AppResourceTagLib")
        AppResourceTagLib parent

        def getGeneralEventImageRelativeUrl(def eventId) {
            return "${RESOURCES_PATH.GENERAL_EVENT}/event-$eventId/images/"
        }

        def getVenueLocationImageRelativeUrl(def venueLocationId) {
            return "${RESOURCES_PATH.GENERAL_EVENT_VENUE_LOCATION}/location-$venueLocationId/"
        }
}
package com.webcommander.plugin.event_management

import com.webcommander.plugin.event_management.webmarketing.EventService

class EventManagementApplicationTagLib {

    EventService eventService

    static namespace = "eventManagementApp"

    def adminJSs = {attrs, body ->
        out << body()
        def url = "plugins/event-management/js/tabs"
        out << app.javascript(src: "$url/event-tab.event.js")
        out << app.javascript(src: "$url/event-tab.event-session.js")
        out << app.javascript(src: "$url/event-tab.event-session-topic.js")
        out << app.javascript(src: "$url/event-tab.calendar.js")
        out << app.javascript(src: "$url/event-tab.equipment.js")
        out << app.javascript(src: "$url/event-tab.venue.js")
        out << app.javascript(src: "$url/event-tab.manage-location.js")
        out << app.javascript(src: "$url/event-tab.manage-ticket.js")
        out << app.javascript(src: "plugins/event-management/fullcalendar/fullcalendar.min.js")
    }

    def adminCSSs = {attrs, body ->
        out << app.stylesheet(href: "plugins/event-management/fullcalendar/fullcalendar.css")
    }

    def decimalToAlphabet = { attrs, body ->
        out << eventService.decimalToAlphabet(attrs.number)
    }
}
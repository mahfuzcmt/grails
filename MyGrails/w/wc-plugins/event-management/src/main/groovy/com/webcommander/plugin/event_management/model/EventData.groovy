package com.webcommander.plugin.event_management.model

import com.webcommander.JSONSerializable
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.EventSession
import com.webcommander.util.AppUtil

class EventData extends JSONSerializable {

    Integer id
    String type
    String title
    Date start
    Date end
    String ticketPrice
    String summary = ""
    Boolean allDay
    String image
    String file
    String url

    public EventData(Event event) {
        id = event.id
        type = "event"
        title = event.name
        start = event.startTime ? setTimeZone(event.startTime) : null
        end = event.endTime ? setTimeZone(event.endTime) : null
        ticketPrice = event.isPurchasable ? event.ticketPriceWithCurrency() : 0
        summary = shortSummary(event)
        image = event.images[0]?.name
        file = event.file
        allDay = false
    }

    public EventData(EventSession session) {
        id = session.event.id
        type = "session"
        title = session.event.name + ": " + session.name
        start = session.startTime ? setTimeZone(session.startTime) : null
        end = session.endTime ? setTimeZone(session.endTime) : null
        ticketPrice = session.event.isPurchasable ? session.ticketPriceWithCurrency() : 0
        summary = shortSummary(session)
        image = session.event.images[0]?.name
        file = session.event.file
        allDay = false
    }

    private static Date setTimeZone(Date time) {
        return time.toZone(AppUtil.session.timezone)
    }

    private static String shortSummary(Event event) {
        String summary = event.summary?.size() > 0 ? event.summary : event.description
        return summary?.size() <= 250 ? summary : summary.substring(0, 249) + '...'
    }

    private static String shortSummary(EventSession eventSession) {
        String summary = eventSession.description?.size() > 0 ? eventSession.description : shortSummary(eventSession.event)
        return summary?.size() <= 250 ? summary : summary.substring(0, 249) + '...'
    }
}

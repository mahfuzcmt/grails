package com.webcommander.plugin.simplified_event_management.model

import com.webcommander.JSONSerializable
import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import com.webcommander.util.AppUtil

/**
 * Created by arman on 11/12/2015.
 */
class SimplifiedEventData extends JSONSerializable{
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

    public SimplifiedEventData(SimplifiedEvent event) {
        id = event.id
        type = "simplified_event"
        title = event.name
        start = event.startTime ? setTimeZone(event.startTime) : null
        end = event.endTime ? setTimeZone(event.endTime) : null
        ticketPrice = event.ticketPriceWithCurrency()
        summary = shortSummary(event)
        image = event.images[0]?.name
        file = event.file
        allDay = false
    }

    private static Date setTimeZone(Date time) {
        return time.toZone(AppUtil.session.timezone)
    }

    private static String shortSummary(SimplifiedEvent event) {
        String summary = event.summary?.size() > 0 ? event.summary : event.description
        return summary?.size() <= 250 ? summary : summary.substring(0, 249) + '...'
    }
}

package com.webcommander.plugin.general_event.model

/**
 * Created by arman on 1/3/2016.
 */
import com.webcommander.JSONSerializable
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.GeneralEventService
import com.webcommander.plugin.general_event.RecurringEvents
import com.webcommander.util.AppUtil

class GeneralEventData extends JSONSerializable{
    Integer id
    String type
    Boolean isRecurring
    String title
    Date start
    Date end
    String ticketPrice
    String summary = ""
    Boolean allDay
    String image
    String file
    String url

    public GeneralEventData(GeneralEvent generalEvent, RecurringEvents recurringEvent = null) {
        id = generalEvent.id
        type = "general_event"
        if(generalEvent.isRecurring.toBoolean()) {
            id = recurringEvent.id
            start  = recurringEvent.start ? setTimeZone(recurringEvent.start) : null
            end = recurringEvent.end ? setTimeZone(recurringEvent.end) : null
            title = generalEvent.name + ' - ' + start
            isRecurring = true
        }else {
            start = generalEvent.startDateTime ? setTimeZone(generalEvent.startDateTime) : null
            end = generalEvent.endDateTime ? setTimeZone(generalEvent.endDateTime) : null
            title = generalEvent.name
            isRecurring = false
        }
        ticketPrice = generalEvent.ticketPriceWithCurrency()
        summary = shortSummary(generalEvent)
        image = generalEvent.images[0]?.name
        file = generalEvent.file
        allDay = false
    }

    private static String shortSummary(GeneralEvent event) {
        String summary = event.summary?.size() > 0 ? event.summary : event.description
        return summary?.size() <= 250 ? summary : summary.substring(0, 249) + '...'
    }

    private static Date setTimeZone(Date time) {
        return time.toZone(AppUtil.session.timezone)
    }
}

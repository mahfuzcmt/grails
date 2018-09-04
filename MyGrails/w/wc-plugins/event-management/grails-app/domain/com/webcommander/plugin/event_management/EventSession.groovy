package com.webcommander.plugin.event_management

import com.webcommander.admin.Operator
import com.webcommander.plugin.event_management.webmarketing.EventService
import grails.util.Holders

class EventSession {

    Long id

    String name
    String description
    String file

    Date startTime
    Date endTime
    Event event
    VenueLocation venueLocation
    Equipment equipment
    Operator createdBy

    Collection<EventSessionTopic> topics = []

    static hasMany = [topics: EventSessionTopic]
    static belongsTo = [event: Event]

    static constraints = {
        description nullable: true
        venueLocation(nullable: true)
        equipment(nullable: true)
        file(nullable: true)
    }

    static mapping = {
        description type: "text"
    }

    public Double lowestTicketPrice() {
        EventService eventService = Holders.grailsApplication.mainContext.getBean(EventService)
        return eventService.getLowestTicketPrice(this)
    }

    public Double highestTicketPrice() {
        EventService eventService = Holders.grailsApplication.mainContext.getBean(EventService)
        return eventService.getHighestTicketPrice(this)
    }

    public String ticketPriceWithCurrency() {
        EventService eventService = Holders.grailsApplication.mainContext.getBean(EventService)
        return eventService.ticketPriceWithCurrency(this)
    }

}

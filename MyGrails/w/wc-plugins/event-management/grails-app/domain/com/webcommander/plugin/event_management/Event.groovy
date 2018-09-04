package com.webcommander.plugin.event_management

import com.webcommander.admin.Operator
import com.webcommander.common.MetaTag
import com.webcommander.plugin.event_management.webmarketing.EventService
import com.webcommander.webcommerce.TaxProfile
import grails.util.Holders

class Event {
    Long id

    String name
    String summary
    String description
    String file
    String title
    String heading

    Boolean isPublic
    Boolean isPurchasable
    Boolean disableGooglePageTracking = false

    Date startTime
    Date endTime
    Date created
    Date updated

    Operator organiser
    Operator createdBy

    VenueLocation venueLocation
    Equipment equipment
    TaxProfile taxProfile

    Collection<EventImage> images = []
    Collection<EventSession> eventSessions = []
    Collection<MetaTag> metaTags = []

    static hasMany = [eventSessions: EventSession, images: EventImage, metaTags: MetaTag]

    static constraints = {
        summary(nullable: true, maxSize: 500)
        description(nullable: true)
        title(nullable: true, maxSize: 200)
        heading(nullable: true, maxSize: 200)
        venueLocation(nullable: true)
        equipment(nullable: true)
        startTime (nullable: true)
        endTime (nullable: true)
        file(nullable: true)
        taxProfile(nullable: true)
    }

    static mapping = {
        description type: "text"
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
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

    public Date calculatedStartTime() {
        EventService eventService = Holders.grailsApplication.mainContext.getBean(EventService)
        Long count = eventService.countUpcomingEventSessions(this)
        if(count > 0) {
            return eventService.getStartTimeFromUpcomingSessions(this)
        }
        return this.startTime
    }

}

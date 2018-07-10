package com.webcommander.plugin.simplified_event_management

import com.webcommander.common.MetaTag
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService
import com.webcommander.webcommerce.TaxProfile
import grails.util.Holders

class SimplifiedEvent {

    Long id

    String name
    String summary
    String description
    String file
    String title
    String heading
    String address

    Double ticketPrice
    Double latitude
    Double longitude
    Integer maxTicket
    Integer maxTicketPerPerson
    Integer totalSoldTicket = 0

    Boolean isPublic
    Boolean showGoogleMap

    Date startTime
    Date endTime
    Date created
    Date updated

    TaxProfile taxProfile

    Collection<SimplifiedEventImage> images = []
    Collection<MetaTag> metaTags = []
    Collection<TicketInventorAdjustment> inventorAdjustments = []

    static hasMany = [images: SimplifiedEventImage, metaTags: MetaTag, inventorAdjustments: TicketInventorAdjustment]

    static constraints = {
        summary(nullable: true, maxSize: 500)
        description(nullable: true)
        title(nullable: true, maxSize: 200)
        heading(nullable: true, maxSize: 200)
        startTime (nullable: true)
        endTime (nullable: true)
        file(nullable: true)
        address(nullable: true, maxSize: 500)
        latitude(nullable: true)
        longitude(nullable: true)
        taxProfile(nullable: true)
        maxTicketPerPerson(nullable: true)
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

    public String ticketPriceWithCurrency() {
        SimplifiedEventService simplifiedEventService = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
        return simplifiedEventService.ticketPriceWithCurrency(this)
    }
}

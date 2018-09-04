package com.webcommander.plugin.event_management

import com.webcommander.admin.Operator

class VenueLocation {

    Long id
    String name

    String description
    String url
    Operator organiser
    Venue venue

    Collection<VenueLocationImage> images = []
    Collection<VenueLocationSection> sections = []
    Collection<VenueLocationInvitation> venueLocationInvitation = []

    static hasMany = [sections: VenueLocationSection, images: VenueLocationImage, venueLocationInvitation: VenueLocationInvitation]

    static constraints = {
        description(nullable: true)
        images(nullable: true)
        venueLocationInvitation(nullable: true)
    }

    static mapping = {
        autoImport false
        description type: "text"
    }
}
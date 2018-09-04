package com.webcommander.plugin.general_event

class VenueLocation {

    Long id
    String name

    String description
    String url
    Venue venue

    Collection<VenueLocationImage> images = []
    Collection<VenueLocationSection> sections = []

    static hasMany = [sections: VenueLocationSection, images: VenueLocationImage]

    static constraints = {
        description(nullable: true)
        images(nullable: true)
    }
    static mapping = {
        table("general_event_venue_location")
        autoImport false
        description type: "text"
    }
}

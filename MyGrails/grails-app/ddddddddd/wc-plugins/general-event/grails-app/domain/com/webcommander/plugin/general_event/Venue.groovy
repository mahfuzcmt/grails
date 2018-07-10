package com.webcommander.plugin.general_event


class Venue {

    Long id

    String name
    String description
    String generalAddress
    String url
    String siteUrl

    Double latitude
    Double longitude
    Boolean showGoogleMap

    Collection<VenueLocation> locations = []
    static hasMany = [locations: VenueLocation]

    static constraints = {
        name(unique: true)
        description(nullable: true)
        generalAddress(nullable: true, maxSize: 500)
        latitude(nullable: true)
        longitude(nullable: true)
        url(nullable: true)
        siteUrl(nullable: true)
        showGoogleMap(nullable: true)
        locations(nullable: true)
    }

    static mapping = {
        autoImport false
        table("general_event_venue")
        description type: "text"
    }

}

package com.webcommander.plugin.event_management

import com.webcommander.admin.Operator

class Venue {

    Long id

    String name
    String description
    String address
    String url
    String siteUrl

    Double latitude
    Double longitude
    Operator manager
    Boolean showGoogleMap

    Collection<VenueLocation> locations = []
    static hasMany = [locations: VenueLocation]

    static constraints = {
        description(nullable: true)
        address(nullable: true, maxSize: 500)
        latitude(nullable: true)
        longitude(nullable: true)
        siteUrl(nullable: true)
        manager(nullable: false)
    }

    static mapping = {
        autoImport false
        description type: "text"
    }

}
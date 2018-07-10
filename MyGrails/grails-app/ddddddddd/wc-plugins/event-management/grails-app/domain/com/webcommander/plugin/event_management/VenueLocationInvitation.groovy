package com.webcommander.plugin.event_management

class VenueLocationInvitation {
    Long id
    String status // pending , approved, rejected

    static belongsTo = [location: VenueLocation, event: Event, eventSession: EventSession]

    static constraints = {
        event(nullable: true)
        eventSession(nullable: true)
    }
}

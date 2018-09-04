package com.webcommander.plugin.event_management

class VenueLocationSection {

    Long id

    String name
    String ticketName
    String rowPrefixType = "alphabetic"
    String rowPrefixOrder
    String rowPrefixStartsAt = "A"
    String columnPrefixType = "numeric"
    String columnPrefixOrder
    String columnPrefixStartsAt = "1"

    Double ticketPrice

    Integer rowCount = 1
    Integer columnCount = 1
    Integer rowAccessBetween
    Integer columnAccessBetween

    Collection<EventTicket> ticket = []

    static hasMany = [ticket: EventTicket]

    static belongsTo = [venueLocation: VenueLocation]

    static mapping = {
        autoImport false
        rowCount defaultValue: 1
        columnCount defaultValue: 1
    }

    static constraints = {
        rowAccessBetween(nullable: true)
        columnAccessBetween(nullable: true)
        ticketName(nullable: true)
        ticketPrice(nullable: true)
        ticket(nullable: true)
    }
}

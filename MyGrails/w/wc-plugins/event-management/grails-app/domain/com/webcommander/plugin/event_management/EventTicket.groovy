package com.webcommander.plugin.event_management

class EventTicket {

    Long id
    Long orderRef

    Boolean isHonorable
    Boolean isReserved = false

    Integer seatNumber
    String ticketNumber

    Date purchased

    static belongsTo = [section: VenueLocationSection, event: Event, session: EventSession]

    static constraints = {
        event(nullable: true)
        session(nullable: true)
        orderRef(nullable: true)
    }

}

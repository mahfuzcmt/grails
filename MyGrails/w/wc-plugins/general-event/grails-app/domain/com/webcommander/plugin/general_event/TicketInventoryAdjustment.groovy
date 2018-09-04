package com.webcommander.plugin.general_event

import com.webcommander.webcommerce.Order

class TicketInventoryAdjustment {
    Long id
    Integer changeQuantity
    String note
    String ticketNumber

    Date created

    Order order

    Collection<Integer> seatNumber = []

    static belongsTo = [generalEvent: GeneralEvent, recurringEvent: RecurringEvents, section: VenueLocationSection]

    static hasMany = [seatNumber: Integer]

    static constraints = {
        order(nullable: true)
        generalEvent(nullable: true)
        recurringEvent(nullable: true)
        section(nullable: true)
        seatNumber(nullable: true)
        ticketNumber(nullable: true)
    }
    static mapping = {
        table("general_event_ticket_inventory_adjustment")
        seatNumber joinTable:[name: "general_event_ticket_inventory_adjustment_seat_number", key: "adjustment_id", column: "seat_number", type: "bigint(20)"]
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }
}

package com.webcommander.plugin.simplified_event_management

import com.webcommander.webcommerce.Order

class TicketInventorAdjustment {
    Long id
    Integer changeQuantity
    String note

    Date created

    Order order

    static belongsTo = [event: SimplifiedEvent]

    static constraints = {
        order(nullable: true)
    }
    static mapping = {
        table("simplified_event_ticket_inventory_adjustment")
    }

    def beforeValidate(){
        if(!created){
            created = new Date().gmt()
        }
    }
}

package com.webcommander.plugin.simplified_event_management

import com.webcommander.webcommerce.Order

class SimplifiedEventCustomFieldData {

    Long id
    String fieldName
    String fieldValue
    String ticket

    Order order
    SimplifiedEvent event

    static belongsTo = [order: Order, event: SimplifiedEvent]

    static constraints = {
        fieldName(blank: false)
        fieldValue(nullable: true)
    }

    static mapping = {
        fieldValue type: "text"
    }
}

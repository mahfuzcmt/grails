package com.webcommander.plugin.general_event

import com.webcommander.events.AppEventManager
import com.webcommander.webcommerce.Order

class GeneralEventCustomFieldData {

    Long id
    String fieldName
    String fieldValue
    String ticket

    Order order
    GeneralEvent generalEvent
    RecurringEvents recurringEvent

    static belongsTo = [order: Order, generalEvent: GeneralEvent, recurringEvent: RecurringEvents]

    static constraints = {
        fieldName(blank: false)
        fieldValue(nullable: true)
        generalEvent(nullable: true)
        recurringEvent(nullable: true)
    }

    static mapping = {
        fieldValue type: "text"
    }

}

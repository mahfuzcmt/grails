package com.webcommander.plugin.general_event

class RecurringEvents {

    Long id
    Date start
    Date end
    Integer totalSoldTicket = 0

    GeneralEvent parentEvent

    static belongsTo = [parentEvent: GeneralEvent]

    static mapping = {
        table("general_event_recurring_events")
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof GeneralEvent)) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }
}

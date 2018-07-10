package com.webcommander.plugin.event_management

class EventSessionTopic {

    Long id
    String name
    String description

    Date created
    Date updated

    static belongsTo = [eventSession: EventSession]

    static constraints = {
        description(nullable: true)
    }

    static mapping = {
        description type: "text"
    }
}

package com.webcommander.plugin.event_management

class EquipmentInvitation {

    Long id
    String status // pending , approved, rejected

    Event event
    EventSession eventSession
    static belongsTo = [equipment: Equipment]

    static constraints = {
        event(nullable: true)
        eventSession(nullable: true)
    }

}

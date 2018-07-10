package com.webcommander.plugin.event_management


class EquipmentType {
    Long id

    String name
    String description
    Collection<Equipment> equipments = []

    static hasMany = [equipments: Equipment]
    static constraints = {
        equipments(nullable: true)
        description(nullable: true, maxSize: 500)
    }

    static mapping = {
        autoImport false
        description length: 500
    }
}

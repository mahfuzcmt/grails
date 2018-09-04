package com.webcommander.plugin.event_management

import com.webcommander.admin.Operator

class Equipment {
    Long id

    String name
    String description
    Boolean autoAccept
    Operator organiser

    Date created
    Date updated

    EquipmentType type
    Collection<EquipmentInvitation> invitation = []

    static hasMany = [invitation: EquipmentInvitation]
    static constraints = {
        description(nullable: true, maxSize: 500)
    }

    static mapping = {
        autoImport false
        description length: 500
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

}

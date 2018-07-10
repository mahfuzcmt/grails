package com.webcommander.plugin.general_event

class Equipment {
    Long id

    String name
    String description

    Date created
    Date updated

    static constraints = {
        description(nullable: true)
    }

    static mapping = {
        autoImport false
        table("general_event_equipment")
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

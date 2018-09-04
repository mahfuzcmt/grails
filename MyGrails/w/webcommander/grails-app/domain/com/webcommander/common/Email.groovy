package com.webcommander.common

class Email {

    Long id
    String name
    String email

    Date created
    Date updated

    static constraints = {
        name(nullable: true)
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

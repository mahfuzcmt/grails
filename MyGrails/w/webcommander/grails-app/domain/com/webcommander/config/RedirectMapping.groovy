package com.webcommander.config

class RedirectMapping {

    Long id
    String scheme
    String host
    String path
    String oldUrl
    String newUrl

    Date created
    Date updated

    static constraints = {
        scheme(nullable: true)
        host(nullable: true)
        path unique: ["scheme", "host"];
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

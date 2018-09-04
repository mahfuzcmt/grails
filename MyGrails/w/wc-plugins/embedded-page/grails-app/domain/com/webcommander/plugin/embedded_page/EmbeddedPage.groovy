package com.webcommander.plugin.embedded_page

import com.webcommander.admin.Operator

class EmbeddedPage {
    Long id

    String name
    String body = ""
    String js
    String css
    String domId

    Boolean isDisposable = false

    Date created
    Date updated

    Operator createdBy

    static constraints = {
        name(unique: true, size: 2..100)
        domId(size: 2..100)
        js(nullable: true)
        css nullable: true
        createdBy(nullable: true)
    }

    static mapping = {
        body type: "text"
        css type: "text"
        name length: 100
        js length: 10000
        domId length: 100
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("Page: " + this.id).hashCode();
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof EmbeddedPage)) {
            return false;
        }
        if (id && obj.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

}

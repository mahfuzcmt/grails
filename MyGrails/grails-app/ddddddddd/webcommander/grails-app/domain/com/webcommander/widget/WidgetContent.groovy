package com.webcommander.widget

import com.webcommander.JSONSerializable

class WidgetContent extends JSONSerializable {

    Long id
    Long contentId
    String type
    String extraProperties

    Date created
    Date updated

    static belongsTo = [widget: Widget]

    static transients = ["errors"]

    static constraints = {
        extraProperties nullable: true, maxSize: 500
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

    @Override
    boolean equals(Object obj) {
        return obj instanceof WidgetContent && obj.id == this.id
    }

    @Override
    int hashCode() {
        return id ? id.intValue() : super.hashCode()
    }
}

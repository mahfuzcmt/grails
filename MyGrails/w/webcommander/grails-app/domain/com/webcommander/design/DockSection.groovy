package com.webcommander.design

import com.webcommander.widget.Widget

class DockSection {

    Long id
    String uuid
    String css

    Date created
    Date updated

    Collection<Widget> widgets = []

    static hasMany = [widgets: Widget]

    static constraints = {
        uuid(unique: true)
    }
    static mapping = {
        css length: 2000
    }

    boolean equals(Object obj) {
        if (!obj instanceof DockSection) {
            return false;
        }
        if (this.id && obj.id) {
            return id == obj.id
        }
        if (this.uuid && obj.uuid) {
            return this.uuid == obj.uuid;
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("dockSection: " + this.id).hashCode();
        }
        return super.hashCode()
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

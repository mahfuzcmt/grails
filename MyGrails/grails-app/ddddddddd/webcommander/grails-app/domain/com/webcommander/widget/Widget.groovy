package com.webcommander.widget

import com.webcommander.JSONSerializable

class Widget extends JSONSerializable {

    Long id
    Long containerId
    String uuid
    String containerType
    String content
    String params
    String widgetType
    String js
    String css
    String clazz
    String title
    String groupId

    Collection<WidgetContent> widgetContent = []

    static hasMany = [widgetContent: WidgetContent]

    Date created
    Date updated

    static transients = ["errors"]

    static constraints = {
        params(nullable: true, maxSize: 1500)
        js(nullable: true)
        css(nullable: true)
        clazz(nullable: true)
        content(nullable: true)
        uuid unique: true
        title nullable: true
        groupId nullable: true
    }

    static mapping = {
        css length: 2000
        js length: 2000
        content length: 10000
        params length: 1500
        uuid length: 40
        widgetContent sort: 'id', order: 'asc', cache: true
    }

    def beforeValidate() {
        //GRAILS 2.3.0 -> beforeValidate does not cascade
        widgetContent?.each {
            it.beforeValidate()
        }
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
        if (!obj instanceof Widget) {
            return false
        }
        if (this.id && obj.id) {
            return id == obj.id
        }
        if (this.uuid && obj.uuid) {
            return this.uuid == obj.uuid
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("widget: " + this.id).hashCode()
        }
        if (this.uuid) {
            return ("widget: " + this.uuid).hashCode()
        }
        return super.hashCode()
    }
}

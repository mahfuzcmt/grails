package com.webcommander.plugin.filter

/**
 * Created by sharif ul islam on 09/04/2018.
 */
class FilterGroup {
    Long id

    Boolean isActive = true

    String name
    String description

    Date created
    Date updated

    Collection<FilterGroupItem> items = []

    static hasMany = [items: FilterGroupItem]

    static constraints = {
        name unique: true, maxSize: 100
        items nullable: true
        description nullable: true
    }

    static mapping = {
        description type: "text"
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

    public static void initialize() {
        if(!FilterGroup.count()) {
            new FilterGroup(name: "Brand", description: "Brand", isActive: true).save()
            new FilterGroup(name: "Manufacturer", description: "Manufacturer", isActive: true).save()
        }
    }

}

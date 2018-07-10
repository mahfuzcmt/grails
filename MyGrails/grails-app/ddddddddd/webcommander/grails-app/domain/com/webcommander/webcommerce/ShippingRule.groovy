package com.webcommander.webcommerce

import com.webcommander.admin.Zone

class ShippingRule {

    Long id
    String name
    String description
    Date created
    Date updated

    ShippingPolicy shippingPolicy
    ShippingClass shippingClass
    Collection<Zone> zoneList = []

    static hasMany = [
        zoneList: Zone
    ];

    static copy_reference = ["zoneList", "shippingClass"]

    static constraints = {
        name (blank: false, unique: true, maxSize: 100)
        description (nullable: true, maxSize: 500)
        shippingClass (nullable: true)
        shippingPolicy (nullable: true)
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
    int hashCode() {
        if (id) {
            return ("ShippingRule: " + id).hashCode()
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof ShippingRule) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }
}

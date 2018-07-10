package com.webcommander.plugin.filter

class Filter {

    Long id
    String name
    String property

    Date created
    Date updated

    static constraints = {
        name(unique: true, size: 2..100)
        property(nullable: true)
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
        if(id) {
            return ("filter: " + id).hashCode();
        }
        return super.hashCode();
    }

    @Override
    boolean equals(Object o) {
        if(!(o instanceof Filter)) {
            return false;
        }
        if(id && o.id) {
            return id == o.id
        }
    }

    public static void initialize() {
        if(!Filter.count()) {
            new Filter(name: "Availability", property: "isAvailable").save()
            new Filter(name: "On Sale", property: "isOnSale").save()
            new Filter(name: "Product Condition", property: "productCondition").save()
            new Filter(name: "Price Range", property: "priceRange").save()
        }
    }
}

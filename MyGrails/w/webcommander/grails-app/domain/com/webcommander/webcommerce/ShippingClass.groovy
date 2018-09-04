package com.webcommander.webcommerce


class ShippingClass {

    Long id
    String name
    String description
    Date created
    Date updated

    static constraints = {
        name(unique: true, maxSize: 100)
        description(nullable: true, maxSize: 255)
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
        if(!ShippingClass.count()) {
            new ShippingClass(name: "Standard", description: "Standard").save()
            new ShippingClass(name: "Express", description: "Express").save()
        }
    }
}

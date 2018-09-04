package com.webcommander.webcommerce

class Shipment {

    Long id

    Date created
    Date updated

    String method //AusPost, Others
    String trackingInfo
    Date shippingDate

    Collection<ShipmentItem> shipmentItem = []

    static hasMany = [shipmentItem: ShipmentItem]
    static belongsTo = [order: Order]

    static constraints = {
        trackingInfo(nullable: true)
    }

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }
}

package com.webcommander.webcommerce

class ShipmentItem {

    Long id
    Integer quantity
    String trackingInfo

    static belongsTo = [orderItem: OrderItem, shipment: Shipment]

    static constraints = {
        trackingInfo(nullable: true)
    }

    static mapping = {
        trackingInfo(type: "text")
    }
}

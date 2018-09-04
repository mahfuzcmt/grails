package com.webcommander.webcommerce

class ShipmentChangeHistory {
    Long id
    String uuid
    String historyLabel //Changes 1 - 18 July, 2017 8:00PM

    Integer previousQuantity
    Integer changedQuantity

    String previousMethod
    String changedMethod

    String previousTrack
    String changedTrack

    Date previousDate
    Date changedDate

    String changeNote

    static belongsTo = [orderItem: OrderItem, shipment: Shipment]

    static constraints = {
        historyLabel(maxSize: 100)
        changeNote(maxSize: 500)
        previousTrack(nullable: true)
        changedTrack(nullable: true)
    }
}

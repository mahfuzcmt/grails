package com.webcommander.plugin.variation

import com.webcommander.webcommerce.OrderItem

class OrderVariationItem {
    Long variationId
    String variationModel

    static belongsTo = [orderItem: OrderItem]

    static constraints = {
        orderItem unique: true
    }
}

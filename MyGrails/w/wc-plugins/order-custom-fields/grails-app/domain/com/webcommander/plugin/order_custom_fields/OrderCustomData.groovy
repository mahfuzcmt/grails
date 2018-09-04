package com.webcommander.plugin.order_custom_fields

import com.webcommander.webcommerce.Order

class OrderCustomData {
    Long id
    String fieldName
    String fieldValue

    Order order

    static belongsTo = [order: Order]

    static constraints = {
        fieldName(blank: false, maxSize: 200)
        fieldValue(nullable: true)
    }

    static mapping = {
        fieldValue type: "text"
    }
}

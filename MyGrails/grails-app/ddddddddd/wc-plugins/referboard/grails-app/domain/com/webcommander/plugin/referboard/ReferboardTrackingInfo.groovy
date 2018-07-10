package com.webcommander.plugin.referboard

import com.webcommander.webcommerce.Order

class ReferboardTrackingInfo {
    Long id
    Order order

    String jsonData

    static mapping = {
        jsonData type: "text"
    }

    static constraints = {
        order unique: true
    }

}

package com.webcommander.plugin.ship_bob

import com.webcommander.webcommerce.Order

class ShipBobTrack {
    String payload
    Order order

    static constraints = {
        order unique: true
    }
}

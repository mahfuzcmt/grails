package com.webcommander.plugin.loyalty_point

/**
 * Created by tariq on 31-Jul-17.
 */
class LoyaltyPointOnShareHistory {
    Long orderId
    Long productId
    Long sharingCustomerId
    String shareMedium
    String shareId
    boolean isUsed = false

    static constraints = {
        productId(nullable: false)
        orderId(nullable: false)
        sharingCustomerId(nullable: false)
        shareMedium(nullable: false)
        shareId(nullable: true)
    }
}

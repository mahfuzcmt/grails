package com.webcommander.plugin.star_track

import com.webcommander.webcommerce.ShippingCondition

class ShippingPolicyExtension {
    Boolean includeFuelSurcharge = false
    Boolean includeSecuritySurcharge = false
    Boolean includeTransitWarranty = false
    Double transitWarrantyValue

    static belongsTo = [shippingCondition: ShippingCondition]

    static constraints = {
        transitWarrantyValue(nullable: true)
        shippingCondition(unique: true)
    }
}

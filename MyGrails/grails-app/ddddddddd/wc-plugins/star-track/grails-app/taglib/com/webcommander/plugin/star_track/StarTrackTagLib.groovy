package com.webcommander.plugin.star_track

import com.webcommander.plugin.star_track.ShippingPolicyExtension
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy

class StarTrackTagLib {
    static namespace = "starTrack"

    def shippingRateApiBlock = { attrs, body ->
        out << body();
        ShippingPolicy policy = pageScope.rate
        ShippingCondition condition = policy?.conditions[0];
        ShippingPolicyExtension extension = condition ? ShippingPolicyExtension.findOrCreateByShippingCondition(condition) : new ShippingPolicyExtension();
        out << g.include(view: "/plugins/star_track/admin/rateInfoEdit.gsp", model: [condition: condition, extension: extension]).toString()
    }
}

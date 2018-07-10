package com.webcommander.plugin.star_track.calculator

import com.webcommander.conversion.LengthConversions
import com.webcommander.conversion.MassConversions
import com.webcommander.models.AddressData
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy

/**
 * Created by sajedur on 5/28/2015.
 */
class ApiShippingCalculator {
    Map calculateStarTrackShippingCost(CartItem cartItem, List<CartItem> applicableCartItems, ShippingPolicy policy, AddressData shippingAddress, Map ruleCache) {
        ShippingCondition condition = policy.conditions[0]
        boolean isDomestic = shippingAddress.countryCode == "AU"
        if(isDomestic && isApplicableForStarTrack(cartItem)) {
            try {
                CartObject object = cartItem.object
                Double l = LengthConversions.convertSIToLength(LengthConversions.LengthType.METERS.toString(), object.length).doubleValue()
                Double w = LengthConversions.convertSIToLength(LengthConversions.LengthType.METERS.toString(), object.width).doubleValue()
                Double h = LengthConversions.convertSIToLength(LengthConversions.LengthType.METERS.toString(),object.height).doubleValue()
                Double shippingWeight = MassConversions.convertSIToMass(MassConversions.MassType.KILOGRAMS.toString(), object.weight).doubleValue();
                Double cost = StarTrackCalculator.calculateCost(shippingAddress, condition, cartItem.quantity, w, h, l, shippingWeight);
                return [shipping: cost, handling: condition.handlingCost ?: 0]
            } catch (Exception ex) {
                return [shipping: null, handling: 0]
            }
        }
        return [shipping: null, handling: 0]
    }

    Boolean isApplicableForStarTrack(CartItem item) {
        CartObject object = item.object
        if(object.weight && object.length && object.width && object.height && object.weight) {
            return true
        }
        return false
    }
}

package com.webcommander.calculator

import com.webcommander.calculator.packing.algorithm.CombinedPacking
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.conversion.LengthConversions
import com.webcommander.conversion.MassConversions
import com.webcommander.models.AddressData
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy

/**
 * Created by sajedur on 3/31/2015.
 */
class ApiShippingCalculatorService {

    Map calculateShippingCost(CartItem cartItem, List<CartItem> applicableCartItems, ShippingPolicy policy, AddressData shippingAddress, Map ruleCache) {
        ShippingCondition condition = policy.conditions[0]
        return "calculate${condition.apiType.capitalize()}ShippingCost"(cartItem, applicableCartItems, policy, shippingAddress, ruleCache)
    }

    Map calculateAuspostShippingCost(CartItem cartItem, List<CartItem> applicableCartItems, ShippingPolicy policy, AddressData shippingAddress, Map ruleCache) {
        ShippingCondition condition = policy.conditions[0]
        boolean isDomestic = shippingAddress.countryCode == "AU"
        ruleCache.auspostApplicable = findApplicableCartItemsForAuspost(applicableCartItems, isDomestic)
        List<CartItem> applicableItems = ruleCache.auspostApplicable
        if(!(cartItem in applicableItems)) {
            return [shipping: null, handling: 0]
        }
        StoreDetail storeDetail = StoreDetail.first()
        double l = 0.0, w = 0.0, h = 0.0, cost = 0.0
        AusPostCalculator ausPostCalculator
        String fromPost = storeDetail.address.postCode
        String toPost = shippingAddress.postCode
        switch(condition.packingAlgorithm) {
            case DomainConstants.PACKING_ALGORITHM.INDIVIDUAL:
                try {
                    if(isDomestic) {
                        CartObject object = cartItem.object
                        Double shippingWeight = MassConversions.convertSIToMass(MassConversions.MassType.KILOGRAMS.toString(), object.weight).doubleValue()
                        l = LengthConversions.convertSIToLength(LengthConversions.LengthType.CENTIMETERS.toString(), object.length).doubleValue()
                        w = LengthConversions.convertSIToLength(LengthConversions.LengthType.CENTIMETERS.toString(), object.width).doubleValue()
                        h = LengthConversions.convertSIToLength(LengthConversions.LengthType.CENTIMETERS.toString(),object.height).doubleValue()
                        ausPostCalculator = new AusPostCalculator(fromPost, toPost, shippingWeight, l, w, h, condition.apiServiceType, condition.extraCover)
                        cost = ausPostCalculator.getCost() * cartItem.quantity
                    } else {
                        CartObject object = cartItem.object
                        Double shippingWeight = MassConversions.convertSIToMass(MassConversions.MassType.KILOGRAMS.toString(), object.weight).doubleValue()
                        ausPostCalculator = new AusPostCalculator(shippingAddress.countryCode, shippingWeight, condition.apiServiceType, condition.extraCover)
                        cost = ausPostCalculator.getCost() * cartItem.quantity
                    }
                    return [
                            shipping: cost,
                            handling: ShippingCalculator.calculateAmountBasedOnType(condition.handlingCost ?: 0.0, condition.handlingCostType, cartItem.total)
                    ]
                } catch (Exception e) {
                    return [shipping: null, handling: 0]
                }
            case DomainConstants.PACKING_ALGORITHM.COMBINED:
                Map totalCostMap
                Double totalCount = 0.0
                Boolean hasRuleCache = ruleCache.totalCostMap != null
                if(hasRuleCache) {
                    totalCostMap = ruleCache.totalCostMap
                    totalCount = ruleCache.totalCount
                }
                if (!hasRuleCache) {
                    try {
                        if(isDomestic) {
                            List dimensions = CombinedPacking.getDimensionMapList(applicableCartItems)
                            for(Map<String, Double> d : dimensions) {
                                ausPostCalculator = new AusPostCalculator(fromPost, toPost, d.weight, d.length, d.width, d.height, condition.apiServiceType, condition.extraCover)
                                cost += ausPostCalculator.getCost()
                            }
                        } else {
                            List<Double> weights = CombinedPacking.getWeightList(applicableCartItems)
                            for(Double weight : weights) {
                                ausPostCalculator = new AusPostCalculator(shippingAddress.countryCode, weight, condition.apiServiceType, condition.extraCover)
                                cost += ausPostCalculator.getCost()
                            }
                        }
                        totalCostMap = ruleCache.totalCostMap = [shipping: cost, handling: condition.handlingCost ?: 0.0]

                    } catch (Exception ex) {
                        return [shipping: null, handling: 0]
                    }
                }
                return [shipping: totalCostMap.shipping / totalCount * cartItem.object.weight * cartItem.quantity, handling: totalCostMap.handling / totalCount * cartItem.object.weight * cartItem.quantity]
        }
    }

    List<CartItem> findApplicableCartItemsForAuspost(List<CartItem> cartItems, boolean isDomestic) {
        List<CartItem> applicableItems = []
        for(CartItem cartItem : cartItems) {
            CartObject object = cartItem.object
            if(isDomestic) {
                if(object.weight && object.length && object.width && object.height) {
                    applicableItems.add(cartItem)
                }
            } else {
                if(object.weight) {
                    applicableItems.add(cartItem)
                }
            }
        }
        return applicableItems
    }
}

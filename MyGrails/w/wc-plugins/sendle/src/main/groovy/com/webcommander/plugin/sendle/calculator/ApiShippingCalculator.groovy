package com.webcommander.plugin.sendle.calculator

import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy

class ApiShippingCalculator {

    private static final String AUSTRALIA = "AU"

    Map calculateSendleShippingCost(CartItem cartItem, List<CartItem> applicableCartItems, ShippingPolicy policy, AddressData shippingAddress, Map ruleCache) {
        ShippingCondition condition = policy.conditions[0]
        if(isApplicableForSendle(cartItem, shippingAddress)) {
            try {
                Double length = cmToMeter(cartItem.object.length).doubleValue()
                Double width = cmToMeter(cartItem.object.width).doubleValue()
                Double height = cmToMeter(cartItem.object.height).doubleValue()
                Double weight = cartItem.object.weight.doubleValue()
                Double cost = SendleCalculator.calculateCost(shippingAddress, condition, cartItem.quantity, width, height, length, weight)
                return [shipping: cost, handling: condition.handlingCost ?: 0]
            } catch (Exception ex) {
                return [shipping: null, handling: 0]
            }
        }
        return [shipping: null, handling: 0]
    }

    Boolean isApplicableForSendle(CartItem item, AddressData shippingAddress){

        def sendleConstants = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SENDLE)

        CartObject object = item.object

        if(!object.weight){
            return false
        }

        if(shippingAddress.countryCode == AUSTRALIA){
            if(object.weight > (sendleConstants.DOMESTIC_SHIPPING_WEIGHT_LIMIT as Double)){
                return false
            }
            if(object.length && object.width && object.height){
                if(!doObeySideLimit(object, (sendleConstants.DOMESTIC_SHIPPING_SIDE_LIMIT as Double))){
                    return false
                }
                if(calculateVolume(object) > (sendleConstants.DOMESTIC_SHIPPING_VOLUME_LIMIT as Double)){
                    return false
                }
            }
        }else{
            if(object.weight > (sendleConstants.INTERNATIONAL_SHIPPING_WEIGHT_LIMIT as Double)){
                return false
            }
            if(item.total > (sendleConstants.INTERNATIONAL_SHIPPING_VALUE_LIMIT as Double)){
                return false
            }
            if(object.length && object.width && object.height){
                if(!doObeySideLimit(object, getSideLimitForInternationalShipping(object, sendleConstants))){
                    return false
                }
                if(calculateVolume(object) > (sendleConstants.INTERNATIONAL_SHIPPING_VOLUME_LIMIT as Double)){
                    return false
                }
            }
        }
        return true
    }

    Double calculateVolume(CartObject object){
        return object.length * object.width * object.height
    }

    Double getSideLimitForInternationalShipping(CartObject object, def sendleConstants){
        if(object.weight > 0 && object.weight <= (sendleConstants.INTERNATIONAL_SHIPPING_WEIGHT_LIMIT_FOR_LOWER_SIDE_LIMIT as Double)){
            return (sendleConstants.INTERNATIONAL_SHIPPING_SIDE_LIMIT_LOWER as Double)
        }else if(object.weight > (sendleConstants.INTERNATIONAL_SHIPPING_WEIGHT_LIMIT_FOR_LOWER_SIDE_LIMIT as Double) && object.weight <= (sendleConstants.INTERNATIONAL_SHIPPING_WEIGHT_LIMIT as Double)){
            return (sendleConstants.INTERNATIONAL_SHIPPING_SIDE_LIMIT_UPPER as Double)
        }
    }

    Boolean doObeySideLimit(CartObject object, Double sideLimit){
        if(object.length > sideLimit || object.width > sideLimit  || object.height > sideLimit ){
            return false
        }
        return true
    }

    Double cmToMeter(Double cm){
        return cm / 100
    }
}

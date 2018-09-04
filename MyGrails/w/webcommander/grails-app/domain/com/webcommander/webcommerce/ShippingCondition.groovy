package com.webcommander.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class ShippingCondition {
    Double fromAmount;
    Double toAmount;
    Double packetWeight;
    Double handlingCost;
    Double shippingCost;
    String shippingCostType;
    String handlingCostType;
    String apiType;
    String apiServiceType
    Double extraCover
    String packingAlgorithm

    static belongsTo = [shippingPolicy: ShippingPolicy]

    static clone_exclude = ["shippingPolicy"]

    static constraints = {
        fromAmount(nullable: true);
        toAmount(nullable: true);
        packetWeight(nullable: true);
        handlingCost(nullable: true);
        shippingCost(nullable: true);
        shippingCostType(nullable: true)
        handlingCostType(nullable: true)
        apiType(nullable: true);
        apiServiceType(nullable: true);
        extraCover(nullable: true)
        packingAlgorithm(nullable: true)
    }

    String getDisplayHandlingCost(Boolean currencySymbol = false) {
        if(handlingCost) {
            return "${currencySymbol && handlingCostType != DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT ? AppUtil.baseCurrency.symbol : ""}${handlingCost.toAdminPrice()}${handlingCostType == DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT ? "%": ""}"
        }
        return handlingCost
    }

    String getDisplayShippingCost(Boolean currencySymbol = false) {
        if(shippingCost) {
            return "${currencySymbol && shippingCostType != DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT ? AppUtil.baseCurrency.symbol : ""}${shippingCost.toAdminPrice()}${shippingCostType == DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT ? "%": ""}"
        }
        return shippingCost
    }

}

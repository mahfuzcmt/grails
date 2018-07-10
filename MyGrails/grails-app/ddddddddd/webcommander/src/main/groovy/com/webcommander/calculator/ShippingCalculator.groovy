package com.webcommander.calculator
import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ShippableCartObject
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ShippingClass
import com.webcommander.webcommerce.ShippingCondition
import com.webcommander.webcommerce.ShippingPolicy
import com.webcommander.webcommerce.ShippingProfile
import com.webcommander.webcommerce.ShippingRule
import grails.util.Holders

class ShippingCalculator {
    private static ApiShippingCalculatorService _apiShippingCalculatorService
    private static ApiShippingCalculatorService getApiShippingCalculatorService() {
        return _apiShippingCalculatorService ?: (_apiShippingCalculatorService = Holders.grailsApplication.mainContext.getBean(ApiShippingCalculatorService))
    }

    public static Map getShippingCost(Cart cart, AddressData shippingAddress) {
        List<CartItem> shippableItems = cart.cartItemList.findAll { it.isShippable }
        return calcShippingCost(shippableItems, shippingAddress)
    }

    private static ShippingProfile profileLookup(ShippableCartObject object, Map matchedProfileCache) {
        String cacheKey = object.type + "#" + object.id
        ShippingProfile profile = matchedProfileCache[cacheKey]
        if(profile == null) {
            profile = matchedProfileCache[cacheKey] = object.resolveShippingProfile()
        }
        return profile
    }

    /**
     * all cart items passed here must have an object of ShippableCartObject
     * @param cartItems
     * @param profile
     * @param shippingAddress
     * @return
     */
    private static Map calcShippingCost(List<CartItem> cartItems, AddressData shippingAddress) {
        Map<String, Map> cache = [
            matchedProfileCache: new HashMap<String, ShippingProfile>(),
            conditionMatchCache: new HashMap<String, Boolean>(),
            cumulativeRuleCache: new HashMap<String, Map>()
        ]
        Map finalCostMap = [shipping: 0.0, handling: 0.0]
        Double cartTotal = cartItems.sum { it.total }
        Boolean isClassDisabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class") != "true"
        Long defaultClassId = null
        List<ShippingClass> classes = []
        if(!isClassDisabled) {
            classes = ShippingClass.list()
            defaultClassId = Long.parseLong AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "default_shipping_class") ?: "0"
        }
        for(CartItem cartItem : cartItems) {
            if(Thread.currentThread().interrupted) { //as shipment calculator runs in thread
                throw new InterruptedException()
            }
            cartItem.shippingCostMaps = [:]
            ShippingProfile profile = profileLookup cartItem.object, cache.matchedProfileCache
            Map profileCostMap = [shipping: null, handling: 0]
            if(profile == null) {
                finalCostMap.shipping = null
                finalCostMap.handling = 0
            } else if(isClassDisabled) {
                profileCostMap = calcShippingCost(cartItem, profile, shippingAddress, cartItems, cartTotal, cache, isClassDisabled, null, defaultClassId)
            } else {
                classes.each {ShippingClass shippingClass ->
                    Map classCostMap = calcShippingCost(cartItem, profile, shippingAddress, cartItems, cartTotal, cache, isClassDisabled, shippingClass.id, defaultClassId)
                    if(classCostMap.shipping != null) {
                        cartItem.shippingCostMaps[shippingClass.id] = classCostMap
                    }
                }
                if(!cartItem.selectedShippingMethod || cartItem.shippingCostMaps.containsKey(cartItem.selectedShippingMethod)) {
                    Iterator iterator = cartItem.shippingCostMaps.iterator()
                    cartItem.selectedShippingMethod = iterator.hasNext() ? iterator.next().key : null
                }
                if(!cartItem.shippingCostMaps.isEmpty()) {
                    profileCostMap = cartItem.shippingCostMaps[cartItem.selectedShippingMethod] ?: profileCostMap
                }
            }
            if(profileCostMap.shipping == null) {
                finalCostMap.shipping = null
                finalCostMap.handling = 0
            }
            if(finalCostMap.shipping != null) {
                finalCostMap.shipping += profileCostMap.shipping
                finalCostMap.handling += profileCostMap.handling
            }
        }
        return finalCostMap
    }

    private static Map calcShippingCost(CartItem cartItem, ShippingProfile profile, AddressData shippingAddress, List<CartItem> cartItems, Double cartTotal, Map cache, isClassDisabled, classId, defaultClassId) {
        boolean isRuleMatched = false
        Map profileCostMap = [shipping: null, handling: 0]
        for(ShippingRule rule : profile.shippingRules) {
            if(Thread.currentThread().interrupted) { //as shipment calculator runs in thread
                throw new InterruptedException()
            }
            Map costMap
            String cacheKey = isClassDisabled ? rule.id.toString() : classId + "-" + rule.id
            Boolean isConditionMatched = cache.conditionMatchCache[cacheKey]
            ShippingPolicy policy = rule.shippingPolicy
            if(isConditionMatched == null) {
                isConditionMatched = cache.conditionMatchCache[cacheKey] = policy != null && (isClassDisabled || (rule.shippingClass ? rule.shippingClass.id == classId : defaultClassId == classId)) &&
                        AppUtil.matchAddressWithZones(shippingAddress, rule.zoneList);
            }
            if(!isConditionMatched) {
                continue
            }
            if(policy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING || policy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.API || policy.isCumulative) {
                Map ruleCache = cache.cumulativeRuleCache["" + rule.id]
                List applicableCartItems = ruleCache?.applicables
                if(!ruleCache) {
                    ruleCache = cache.cumulativeRuleCache["" + rule.id] = [:]
                    ruleCache.applicables = applicableCartItems = findSameRuleCartItems(rule, cartItems, cache.matchedProfileCache)
                }
                costMap = getCumulativeCostMap(cartItem, applicableCartItems, rule, shippingAddress, ruleCache, cartTotal)
            } else {
                costMap = getNonCumulativeCostMap(cartItem, rule)
            }
            if(costMap.shipping != null) {
                isRuleMatched = true
                if(profile.rulePrecedence == DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHER_PRIORITY) {
                    profileCostMap = costMap
                    if(costMap.shipping >= 0) {
                        break
                    }
                } else if(profile.rulePrecedence == DomainConstants.SHIPPING_PROFILE_RULE_PRECEDENCE.RULES_WITH_HIGHEST_SHIPPING_COST) {
                    profileCostMap = profileCostMap.shipping == null || costMap.shipping > profileCostMap.shipping ? costMap : profileCostMap
                } else {
                    profileCostMap = profileCostMap.shipping == null || costMap.shipping < profileCostMap.shipping ? costMap : profileCostMap
                }
            }
        }
        if(isRuleMatched) {
            return profileCostMap
        }
        return [shipping: null, handling: 0]
    }


    private static Map getFlatRateCost(ShippingPolicy policy, Double relativeAmount) {
        ShippingCondition condition = policy.conditions.first()
        return [
            shipping: calculateAmountBasedOnType(condition.shippingCost, condition.shippingCostType, relativeAmount),
            handling: calculateAmountBasedOnType(condition.handlingCost ?: 0.0, condition.handlingCostType, relativeAmount)
        ]
    }

    private static Map getCostFromRangeConditions(ShippingPolicy policy, Double checkValue, Double relativeAmount, String adjustField = null) {
        Map costMap = policy.conditions.findResult { condition ->
            Double adjustValue = adjustField ? (condition[adjustField] ?: 0.0) : 0.0
            Double value = checkValue + adjustValue
            if (value >= condition.fromAmount && value <= condition.toAmount) {
                return [
                    shipping: calculateAmountBasedOnType(condition.shippingCost, condition.shippingCostType, relativeAmount),
                    handling: calculateAmountBasedOnType(condition.handlingCost ?: 0.0, condition.handlingCostType, relativeAmount)
                ]
            }
        }
        if(!costMap && policy.isAdditional) {
            costMap = calcAdditionalCostForRange policy, checkValue, relativeAmount, adjustField
        }
        return costMap ?: [shipping: null, handling: 0]
    }

    private static Map calculateUnitShippingCost(Map costMap, Double totalByType, Double baseTotal = 1) {
        Map cMap = [shipping: costMap.shipping, handling: costMap.handling]
        if(totalByType != 0 && totalByType != Double.NaN) {
            cMap.shipping = (costMap.shipping / totalByType) * baseTotal
            cMap.handling = (costMap.handling / totalByType) * baseTotal
        }
        return cMap
    }

    static Double calculateAmountBasedOnType(Double amount, String type, Double relativeAmount) {
        if(type == DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT) {
            amount = relativeAmount * amount / 100
        }
        return amount
    }

    private static Map getCumulativeCostMap(CartItem cartItem, List<CartItem> applicableCartItems, ShippingRule rule, AddressData shippingAddress, Map ruleCache, Double cartTotal) {
        Map totalCostMap
        Double totalCount = 0.0
        Boolean hasRuleCache = ruleCache.totalCostMap != null
        if(hasRuleCache) {
            totalCostMap = ruleCache.totalCostMap
            totalCount = ruleCache.totalCount
        }
        ShippingPolicy policy = rule.shippingPolicy
        switch(policy.policyType) {
            case DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING:
                ShippingCondition condition = policy.conditions.first()
                return [shipping: 0.0, handling: calculateAmountBasedOnType(condition.handlingCost ?: 0.0, condition.handlingCostType, cartItem.total)]
            case DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE:
                int size = applicableCartItems.size()
                if(!hasRuleCache) {
                    totalCostMap = ruleCache.totalCostMap = getFlatRateCost(policy, cartTotal)
                }
                return calculateUnitShippingCost(totalCostMap, size)
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT:
                if(!cartItem.object.weight) {
                    return [shipping: null, handling: 0]
                }
                if(!hasRuleCache) {
                    totalCount = ruleCache.totalCount = applicableCartItems.sum { (it.object.weight ?: 0.0) * it.quantity }
                    totalCostMap = ruleCache.totalCostMap = getCostFromRangeConditions policy, totalCount, cartTotal, "packetWeight"
                }
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                return calculateUnitShippingCost(totalCostMap, totalCount, cartItem.object.weight * cartItem.quantity)
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_PRICE:
                if(!hasRuleCache) {
                    def total = applicableCartItems.sum { it.baseTotal }
                    totalCount = ruleCache.totalCount = (policy.isPriceEnterWithTax ? (applicableCartItems.sum{ it.tax } + total) : total)
                    totalCostMap = ruleCache.totalCostMap = getCostFromRangeConditions policy, totalCount, cartTotal
                }
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                Double applicableBaseTotal = cartItem.baseTotal;
                if(policy.isPriceEnterWithTax) {
                    applicableBaseTotal = applicableBaseTotal + cartItem.tax
                }
                return calculateUnitShippingCost(totalCostMap, totalCount, applicableBaseTotal)
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY:
                if(!hasRuleCache) {
                    totalCount = ruleCache.totalCount = applicableCartItems.sum { it.quantity }
                    totalCostMap = ruleCache.totalCostMap = getCostFromRangeConditions policy, totalCount, cartTotal
                }
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                return calculateUnitShippingCost(totalCostMap, totalCount, cartItem.quantity)
            case DomainConstants.SHIPPING_POLICY_TYPE.API:
                return apiShippingCalculatorService.calculateShippingCost(cartItem, applicableCartItems, policy, shippingAddress, ruleCache)
        }
        return [shipping: null, handling: 0]
    }

    private static Map getNonCumulativeCostMap(CartItem cartItem, ShippingRule rule) {
        Map totalCostMap
        Double totalCount
        ShippingPolicy policy = rule.shippingPolicy
        switch(policy.policyType) {
            case DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE:
                totalCostMap = getFlatRateCost(policy, cartItem.unitTotal)
                return [shipping: totalCostMap.shipping * cartItem.quantity, handling: totalCostMap.handling * cartItem.quantity]
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT:
                if(!cartItem.object.weight) {
                    return [shipping: null, handling: 0]
                }
                totalCount = cartItem.object.weight
                totalCostMap = getCostFromRangeConditions policy, totalCount, cartItem.unitTotal, "packetWeight"
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                return [shipping: totalCostMap.shipping * cartItem.quantity, handling: totalCostMap.handling * cartItem.quantity]
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_PRICE:
                totalCount = policy.isPriceEnterWithTax ? (cartItem.unitPrice + cartItem.unitTax) : cartItem.unitPrice
                totalCostMap = getCostFromRangeConditions policy, totalCount, cartItem.unitTotal
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                return [shipping: totalCostMap.shipping * cartItem.quantity, handling: totalCostMap.handling * cartItem.quantity]
            case DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY:
                totalCount = cartItem.quantity
                totalCostMap = getCostFromRangeConditions policy, totalCount, cartItem.total
                if(totalCostMap.shipping == null) {
                    return [shipping: null, handling: 0]
                }
                return [shipping: totalCostMap.shipping, handling: totalCostMap.handling]
        }
        return [shipping: null, handling: 0]
    }

    private static List<CartItem> findSameRuleCartItems(ShippingRule rule, List<CartItem> cartItems, Map<String, ShippingProfile> matchedProfileCache) {
        List<CartItem> applicableCartItems = []
        cartItems.each {
            ShippingProfile profile = profileLookup it.object, matchedProfileCache
            if (profile && profile.shippingRules.contains(rule)) {
                applicableCartItems.add(it)
            }
        }
        return applicableCartItems
    }

    private static Map calcAdditionalCostForRange(ShippingPolicy policy, Double value, Double relativeAmount, String adjustField) {
        List<ShippingCondition> conditions = policy.conditions
        ShippingCondition condition
        Double diff = Double.MAX_VALUE;
        for(ShippingCondition _condition : conditions) {
            Double _diff = value + (adjustField ? _condition[adjustField] ?: 0 : 0) - _condition.toAmount
            if(_diff < diff && _diff > 0) {
                diff = _diff
                condition = _condition
            }
        }
        if(condition) {
            value = value + (adjustField ? condition[adjustField] ?: 0 : 0)
            Double extra = Math.ceil((value - condition.toAmount) / policy.additionalAmount);
            return [
                shipping: calculateAmountBasedOnType(condition.shippingCost, condition.shippingCostType, relativeAmount) + extra * policy.additionalCost,
                handling: calculateAmountBasedOnType(condition.handlingCost ?: 0.0, condition.handlingCostType, relativeAmount)
            ]
        }
        return [shipping: null, handling: 0]
    }
}

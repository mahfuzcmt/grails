package com.webcommander.models

import com.webcommander.calculator.model.Discount
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class Cart {
    Long orderId

    String deliveryType = DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING

    Double cartPageDisplaySubTotal;
    Double checkoutPageDisplaySubTotal = 0.0;
    Double discount;
    Double discountOnShipping = 0.0
    Double discountOnOrder = 0.0
    Double tax;
    Double actualTax = 0
    Double total; // baseTotal + tax - discount
    Double baseTotal; //all price * quantity
    Double paid = 0; //If any partial payment cart

    Map<String, Double> shippingCost = [:];
    Map tagged = [:] //surcharge, payable

    String sessionId

    Boolean isDirty
    Boolean isShipDirty
    Boolean updating //status

    Thread shippingProcessor

    Object selectedDiscountData

    List<CartItem> cartItemList;

    @Deprecated
    List<Discount> shippingDiscounts = []

    Cart() {
        this.cartItemList = new ArrayList<CartItem>();
    }

    void initDeliveryType() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE)
        if(configs.enable_shipping == "true") {
            this.deliveryType = DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING
        } else if(configs.enable_store_pickup == "true") {
            this.deliveryType = DomainConstants.ORDER_DELIVERY_TYPE.STORE_PICKUP
        } else if(configs.enable_others_shipping == "true") {
            this.deliveryType = DomainConstants.ORDER_DELIVERY_TYPE.OTHERS_SHIPPING
        }
    }

    void clearShippingDiscounts() {
        this.discountOnShipping = 0.0;
        Iterator iterator = this.shippingDiscounts.iterator()
        while (iterator.hasNext()) {
            Discount discount = iterator.next()
            discount.onClear()
        }
        this.shippingDiscounts = []
    }

    Boolean addShippingDiscounts(Discount discount) {
        Double shippingCost = shippingCost.shipping
        if(shippingCost == null) return
        Double discountable = shippingCost - this.discountOnShipping
        if(discountable > 0) {
            discountable = discountable < discount.amount ? discountable : discount.amount
            discount.amount = discountable
            this.discountOnShipping += discountable
            discount.onAdd()
            this.shippingDiscounts.add(discount)
            return true
        }
        return false
    }

    Double getDiscount() {
        return discount + discountOnOrder
    }

    Long getUniqueTaxCodeId () {
        if (cartItemList) {
            Long taxCodeId = cartItemList.get(0).object.product.taxCodeId
            for (CartItem cartItem : cartItemList) {
                if (taxCodeId && !taxCodeId.equals(cartItem.object.product.taxCodeId)) {
                    return null
                }
                taxCodeId = cartItem.object.product.taxCodeId
            }
            return taxCodeId
        }
        return null
    }

    Double getTotal() {
        if (actualTax) {
            Double total = (baseTotal - discountOnOrder) + actualTax
            return total
        } else {
            return total - discountOnOrder
        }
    }

    Double getActualTotal() {
        return total
    }

}

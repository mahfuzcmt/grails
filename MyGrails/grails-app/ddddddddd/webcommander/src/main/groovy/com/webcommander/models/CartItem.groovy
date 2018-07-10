package com.webcommander.models

import com.webcommander.calculator.TaxCalculator
import com.webcommander.calculator.model.Discount
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.webcommerce.TaxProfile

class CartItem {

    Integer id
    Integer quantity = 0
    Integer oldQuantity = 0

    Long selectedShippingMethod // Selected Shipping Class

    Boolean isTaxable
    Boolean isShippable
    Boolean isQuantityAdjustable

    Double total; // after tax, discount and quantity. Is managed by CartManager
    Double cartPageDisplayTotal = 0.0; // total that should be displayed (based on config)
    Double checkoutPageDisplayTotal = 0.0; // total that should be displayed (based on config)
    Double unitPrice; // single price
    Double displayUnitPrice // Single price based on config
    Double discount = 0.0;
    Double actualDiscount = 0.0;
    Double baseTotal // (* quantity) price without tax and discount
    Double unitTax = 0
    Double tax = 0 // unitTax * quantity
    Double actualTax = 0 // tax amount after rounding amount
    Double taxDiscount = 0

    Map<Long, Map> shippingCostMaps = [:]

    List<Discount> discounts = []
    List variations

    CartObject object;
    Object discountData;

    CartItem(CartObject object, Integer quantity, Double price, Boolean calcTax = false) {
        id = CartManager.nextItemId
        this.object = object;
        this.quantity = quantity
        this.isTaxable = object instanceof TaxableCartObject;
        this.isShippable = object instanceof ShippableCartObject && object.isShippable
        this.isQuantityAdjustable = object instanceof QuantityAdjustableCartObject
        this.unitPrice = price
        this.baseTotal = this.unitPrice * this.quantity
        if(calcTax) {
            calculateTax()
        }
    }

    CartItem(CartObject object, Integer quantity, Boolean calcTax = true) {
        this(object, quantity, object.effectivePrice, calcTax)
    }

    void calculateTax() {
        if(isTaxable) {
            TaxProfile profile
            if (object.product && object.product.tax >= 0) {
                unitTax = object.product.tax
            } else {
                profile = object.resolveTaxProfile();
                unitTax = TaxCalculator.getTax(profile, discountedUnitPrice , false);
            }

            actualTax = tax = unitTax * quantity - taxDiscount

            /*if(tax && profile) {
                tax = tax.toTax(profile.appliedRule)
            }*/
        }
    }

    void refresh() {
        object.refresh()
        this.unitPrice = object.effectivePrice
        this.baseTotal = this.unitPrice * this.quantity
    }

    void updateQuantity(Integer quantity, List<CartItem> variations = null) {
        HookManager.hook("beforeCartItemQuantityUpdate", this, quantity)
        object.validate(quantity + (variations ? variations.sum {it.quantity} : 0));
        this.quantity = quantity
        baseTotal = unitPrice * quantity
        actualTax = tax = unitTax * quantity
        TaxProfile profile = object.resolveTaxProfile()
        if(tax && profile) {
            tax = tax.toTax(profile.appliedRule)
        }
    }

    Double getUnitTotal() {
        return unitTax + discountedUnitPrice;
    }

    Double getDiscountedUnitPrice() {
        return unitPrice - (discount / quantity);
    }

    Double getDisplayDiscountedUnitPrice() {
        return object?.product?.priceToDisplay - actualDiscount ?: 0.0
    }

    void clearDiscounts() {
        this.discount = 0.0;
        Iterator iterator = this.discounts.iterator()
        while (iterator.hasNext()) {
            Discount discount = iterator.next()
            discount.onClear()
        }
        this.discounts = []
    }

    Boolean addDiscount(Discount discount) {
        Double discountable = baseTotal - this.discount
        if(discountable > 0) {
            discountable = discountable < discount.amount ? discountable : discount.amount
            discount.amount = discountable
            this.discount += discountable
            discount.onAdd()
            this.discounts.add(discount)
            return true
        }
        return false
    }

    Double getDisplayUnitPrice () {
        return object?.product.actualPriceToDisplay ?: 0.0
    }

    Double getActualUnitPrice () {
        return new Double ( (unitPrice + unitTax).toConfigPrice() )
    }

    Boolean isShowActualPrice () {
        Double displayUnitPrice = getDisplayUnitPrice()
        displayUnitPrice = new Double(displayUnitPrice.toConfigPrice())
        if (discount > 0.001 && displayUnitPrice != actualDiscount && (discountData && discountData.isShowActualPrice)) {
            return true
        }
        return false
    }

}

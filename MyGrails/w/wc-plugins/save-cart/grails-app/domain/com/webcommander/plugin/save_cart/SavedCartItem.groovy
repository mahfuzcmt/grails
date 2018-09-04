package com.webcommander.plugin.save_cart

import com.webcommander.admin.Customer
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.models.blueprints.CartItemable
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.TypeConvertingMap

class SavedCartItem implements CartItemable {
    Long id
    Long itemId
    Integer quantity = 0

    String itemType
    String params

    private CartObject object
    private CartItem cartItem

    Collection<String> variations = []
    Long storeId
    static belongsTo = [savedCart: SavedCart]
    static hasMany = [variations: String]

    static constraints = {
        params(nullable: true)
        storeId(nullable: true)
    }

    static mapping = {
        params type: "text"
    }

    static transients = ["cartItem", "object", "paramsObj", "cartItem", "price", "tax", "discount", "baseTotal", "total"]

    CartObject getObject() {
        if(!object) {
            object = HookManager.hook("resolveCartObject-${itemType}", null, this)
        }
        return object
    }

    // TODO: RFD
    CartItem getCartItem() {
        CartObject object = getObject()
        if( object && !cartItem) {
            cartItem = new CartItem(object, quantity)
            Cart _cart = CartManager.createCartByCartItems([cartItem])
            cartItem = _cart.cartItemList.get(0)
            Customer customer = AppUtil.session.customer ? Customer.proxy(AppUtil.session.customer) : null;
//            def discountList = DiscountCalculator.getDiscountAmount([cartItem], customer);
//            def discountTaxProfile = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.DISCOUNT, "tax_profile").toLong(null)
//            Double discountTaxRate = discountTaxProfile ? TaxCalculator.getTaxRate(TaxProfile.proxy(discountTaxProfile)) : 0
//            cartItem.discount = discountList[0] * (1 + discountTaxRate)
        }
        return cartItem
    }

    @Override
    String getItemName() {
        return object ? object.name : itemType
    }

    TypeConvertingMap paramsObj() {
        return params ? new TypeConvertingMap(JSON.parse(params) as Map) : new TypeConvertingMap()
    }

    @Override
    Long getStoreId() {
        return this.storeId
    }

    Double getPrice() {
        return getCartItem().unitPrice
    }

    Double getBaseTotal() {
        return getCartItem().baseTotal
    }

    Double getTax() {
        return getCartItem().tax
    }

    Double getUnitTax() {
        return getCartItem().unitTax
    }

    Double getDiscount() {
        return getCartItem().discount
    }

    Double getTotal() {
        return getCartItem().cartPageDisplayTotal
    }
}

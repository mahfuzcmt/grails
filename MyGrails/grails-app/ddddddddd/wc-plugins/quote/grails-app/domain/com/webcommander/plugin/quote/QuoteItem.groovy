package com.webcommander.plugin.quote

import com.webcommander.manager.HookManager
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.models.blueprints.CartItemable
import grails.converters.JSON
import grails.util.TypeConvertingMap

class QuoteItem implements CartItemable{
    Long id
    Long itemId
    Integer quantity = 0

    String itemType
    String params

    Boolean isTaxable
    Boolean isShippable

    Double price = 0 //unit price
    Double tax = 0
    Double discount = 0

    private CartObject object
    private CartItem cartItem

    Collection<String> variations = []
    static belongsTo = [quote: Quote]
    static hasMany = [variations: String]

    static constraints = {
        params nullable: true
    }

    static mapping = {
        params type: "text"
    }

    static transients = ["cartItem", "object"]

    TypeConvertingMap paramsObj() {
        return params ? new TypeConvertingMap(JSON.parse(params) as Map) : new TypeConvertingMap()
    }

    CartObject getObject() {
        if(!object) {
            object = HookManager.hook("resolveCartObject-${itemType}", null, this)
        }
        return object
    }

    @Override
    String getItemName() {
        return object ? object.name : itemType
    }

    CartItem getCartItem() {
        CartObject object = getObject()
        if( object && !cartItem) {
            cartItem = new CartItem(object, quantity)
        }
        return cartItem
    }

    public Double getTotalPrice() {
        return quantity * price
    }

    public Double getTotalAmount() {
        return quantity * price + tax - discount
    }
}

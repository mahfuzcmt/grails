package com.webcommander.plugin.shipment_calculator.manager

import com.webcommander.manager.HookManager
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by sajed on 4/8/2014.
 */
class ShipmentCalculatorCartManager {
    public static CartItem populateCartItemForProduct(ProductData product, Integer quantity, GrailsParameterMap params) {
        ProductInCart productCart = new ProductInCart(product, params)
        CartItem cartItem = new CartItem(productCart, quantity);
        cartItem.total = cartItem.baseTotal + cartItem.tax - cartItem.discount;
        return HookManager.hook("populateCartItem", cartItem, quantity, product)
    }
}

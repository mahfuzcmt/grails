package com.webcommander.plugin.simplified_event_management.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import com.webcommander.plugin.simplified_event_management.model.CartSimplifiedEventTicket
import com.webcommander.throwables.CartManagerException

class CartController {

    @License(required = "allow_simplified_event_feature")
    def addTicket() {
        Integer seatQuantity = params.("seats").toLong();
        SimplifiedEvent event = SimplifiedEvent.get(params.event.toLong())
        boolean success;
        String error
        CartItem item
        Map errorArgs
        Cart cart
        CartSimplifiedEventTicket ticket = new CartSimplifiedEventTicket(event.id)
        try {
            item = CartManager.addToCart(session.id, ticket, seatQuantity)
            success = true;
        } catch(CartManagerException ex) {
            error = ex.message;
            success = false;
            item = new CartItem(ex.product, 0)
            errorArgs = ex.messageArgs
        }
        cart = CartManager.getCart(session.id);
        render(view: "/site/addToCartPopup", model: [success: success, cartdata: item, quantity: seatQuantity, error: error, cart: cart, totalItem: cart ? cart.cartItemList.size() : 0,
                                                     errorArgs: errorArgs])
    }
}

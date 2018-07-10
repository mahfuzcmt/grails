package com.webcommander.plugin.abandoned_cart.controllers.site

import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.plugin.abandoned_cart.AbandonedCart
import com.webcommander.plugin.abandoned_cart.AbandonedCartService
import grails.converters.JSON

class AbandonedCartController {
    AbandonedCartService abandonedCartService

    @RequiresCustomer
    def loadAbandonedCart() {
        Customer customer = Customer.get(session.customer)
        List<AbandonedCart> abandonedCarts = AbandonedCart.findAllByCustomer(customer)
        render(view: "/plugins/abandoned_cart/site/abandonedCart", model: [carts: abandonedCarts])
    }

    @RequiresCustomer
    def removeAbandonedCart() {
        boolean success = abandonedCartService.removeAbandonedCart(params)
        if(success) {
            render([status: "success", message: g.message(code: "abandoned.cart.successfully.deleted")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "abandoned.cart.delete.fail")] as JSON)
        }
    }

    @RequiresCustomer
    def abandonedAddToCart() {
        Boolean added = abandonedCartService.abandonedAddToCart(params)
        if(added) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "add.to.cart.fail")] as JSON)
        }
    }

    @RequiresCustomer()
    def loadAbandonedCartDetails() {
        AbandonedCart abandonedCart = AbandonedCart.proxy(params.cartId);
        render(view: "/plugins/abandoned_cart/site/abandonedCartDetailsView", model: [cart: abandonedCart])
    }
}

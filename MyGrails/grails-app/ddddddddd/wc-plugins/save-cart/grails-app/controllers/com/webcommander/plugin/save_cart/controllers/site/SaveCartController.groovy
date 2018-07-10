package com.webcommander.plugin.save_cart.controllers.site

import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.plugin.save_cart.SaveCartService
import com.webcommander.plugin.save_cart.SavedCart
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import grails.converters.JSON


class SaveCartController {
    SaveCartService saveCartService
    CommonService commonService
    def beforeSave() {
        String redirectUrl = "/customer/profile?saveCart=true#my-carts"
        if(!session.customer) {
            redirectUrl = "/customer/login?referer=" + URLEncoder.encode(redirectUrl, "utf-8")
        }
        session.save_cart = true
        redirect(uri: redirectUrl)
    }

    @RequiresCustomer
    def customerProfile() {
        if(params.clearSaveOperation) {
            session.save_cart = false
        }
        List<SavedCart> carts = saveCartService.getCarts(session.customer)
        render(template:  "/plugins/save_cart/site/loadSavedCart", model: [carts: carts])
    }

    @RequiresCustomer
    def saveCartInit() {
        render(view: "/plugins/save_cart/site/saveCartInit")
    }

    @RequiresCustomer
    def isUnique() {
        String existenceStatus = SavedCart.createCriteria().list {
            eq("name", params.value)
            eq("customer.id", session.customer)
        } ? "exist" : "not-exist";
        render(commonService.generateResponseForUniqueCheck(existenceStatus, params.field, params.value) as JSON)
    }

    @RequiresCustomer
    def save() {
        Cart cart = CartManager.getCart(session.id)
        Customer customer = Customer.get(session.customer)
        Boolean result = saveCartService.save(params, cart, customer)
        if(result) {
            session.save_cart = false
            render([status: "success", message: g.message(code: "cart.save.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "cart.save.error")] as JSON)
        }
    }

    @RequiresCustomer
    def removeSavedCart() {
        boolean success = saveCartService.removeSavedCart(params)
        if(success) {
            render([status: "success", message: g.message(code: "saved.cart.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "saved.cart.delete.error")] as JSON)
        }
    }

    @RequiresCustomer
    def addToCart() {
        SavedCart savedCart = SavedCart.get(params.cartId)
        List<Map> exceptions = CartManager.addToCart(savedCart.cartItems)
        if(exceptions.size() == 0) {
            render([status: "success"] as JSON)
        } else if(exceptions.size() < savedCart.cartItems.size()) {
            String popup = g.include(view: "/site/cart/cartItemableAddExceptionPopup.gsp", model: [exceptions: exceptions, totalItems: savedCart.cartItems.size()]).toString()
            render([status: "partial", content: popup] as JSON)
        } else {
            render([status: "error", message: g.message(code: "add.to.cart.fail")] as JSON)
        }
    }

    @RequiresCustomer
    def loadSavedCartDetails() {
        SavedCart savedCart = SavedCart.proxy(params.cartId);
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        Map model = [
                config: config,
                cart: savedCart
        ]
        render(view: "/plugins/save_cart/site/savedCartDetails", model: model)
    }
}

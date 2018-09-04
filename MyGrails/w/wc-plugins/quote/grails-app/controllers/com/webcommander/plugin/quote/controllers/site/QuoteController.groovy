package com.webcommander.plugin.quote.controllers.site

import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.plugin.quote.Quote
import com.webcommander.plugin.quote.QuoteService
import com.webcommander.throwables.ApplicationRuntimeException
import grails.converters.JSON


class QuoteController {
    QuoteService quoteService
    CommonService commonService

    def save() {
        String redirectUrl = "/quote/save"
        if(!session.customer) {
            redirectUrl = "/customer/login?referer=" + URLEncoder.encode(redirectUrl, "utf-8")
        } else {
            Customer customer = Customer.get(session.customer)
            if(!session.effective_shipping_address) {
                session.effective_shipping_address = new AddressData(customer.activeShippingAddress)
                AppEventManager.fire("effective-shipping-change", [session.id])
            }
            if(!session.effective_billing_address) {
                session.effective_billing_address = new AddressData(customer.activeBillingAddress)
                AppEventManager.fire("effective-billing-change", [session.id])
            }
            Cart cart = CartManager.getRefreshedCart(session.id)
            String errorMessage;
            Boolean result
            try {
                result = quoteService.save(cart, customer, session.effective_shipping_address, session.effective_billing_address)
            } catch (ApplicationRuntimeException ex){
                errorMessage = ex.message
            } catch(Exception ex) {
                errorMessage = g.message(code: "quote.sent.error")
                log.error(ex.message, ex)
            }
            if(result) {
                flash.model = [message: g.message(code: "quote.sent.success")]
            } else {
                flash.model = [error: errorMessage]
            }
            redirectUrl = "/cart/details"
        }
        redirect(url: redirectUrl)
    }

    @RequiresCustomer
    def saves() {
        Cart cart = CartManager.getCart(session.id)
        Customer customer = Customer.get(session.customer)
        Boolean result = quoteService.save(params, cart, customer)
        if(result) {
            session.save_cart = false
            render([status: "success", message: g.message(code: "cart.save.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code: "cart.save.error")] as JSON)
        }
    }

    @RequiresCustomer
    def removeQuote() {
        boolean success = quoteService.removeQuote(params)
        if(success) {
            render([status: "success", message: g.message(code: "saved.cart.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "saved.cart.delete.error")] as JSON)
        }
    }

    @RequiresCustomer()
    def loadQuoteDetails() {
        Quote savedCart = Quote.proxy(params.cartId);
        render(view: "/plugins/save_cart/site/savedCartDetails", model: [cart: savedCart])
    }
}

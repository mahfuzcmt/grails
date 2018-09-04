package com.webcommander.plugin.quote

import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.models.ProductInCartBase
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.OrderService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Initializable
@Transactional
class QuoteService {
    OrderService orderService
    CommanderMailService commanderMailService
    CommonService commonService

    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            List<Quote> quotes = Quote.createCriteria().list {
                eq("customer.id", id)
            }
            quotes.each {
                AppEventManager.fire("before-quote-delete", [it.id])
                it.delete()
            }
        })

        AppEventManager.on("before-quote-delete", { id ->
            Quote quote = Quote.proxy(id)
            quote?.quoteItems.each {
                it.variations = []
            }*.delete()
        })
        AppEventManager.on("before-product-put-in-trash", { id, at2_reply ->
            QuoteItem.createCriteria().list {
                eq("itemId", id)
                eq("itemType", NamedConstants.CART_OBJECT_TYPES.PRODUCT )
            }*.delete()
        })
    }

    Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                createAlias("customer", "c")
                or {
                    like "c.firstName", "%" + params.searchText.encodeAsLikeText() + "%"
                    like "c.lastName", "%" + params.searchText.encodeAsLikeText() + "%"
                    eq "c.userName", params.searchText
                    if(params.searchText.matches(/^\d+$/)) {
                        eq "id", params.searchText.toLong()
                    }
                }
            }
        }
    }
    List<Quote> getQuotes(Map params) {
        return Quote.createCriteria().list([max: params.max, offset: params.offset]) {
            and getCriteriaClosure(params)
            order(params.sort ?: "id", params.dir ?: "desc")
        }
    }

    Integer getQuoteCount(Map params) {
        return Quote.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    Boolean save(Cart cart, Customer customer, AddressData billing, AddressData shipping) {
        List<CartItem> cartItems = cart?.cartItemList
        if(!cartItems) {
            return false
        }
        Quote quote = new Quote()
        quote.customer = customer
        Boolean shouldHaveShipping = cart.cartItemList.find {
            it.isShippable
        }
        def applicableShippingMap = shouldHaveShipping ? cart.shippingCost : [:]
        if(shouldHaveShipping && (!applicableShippingMap || applicableShippingMap.shipping == null)) {
            throw new ApplicationRuntimeException("ship.location.not.supported")
        }
        quote.billing = orderService.copyAddress(billing);
        quote.shipping = orderService.copyAddress(shipping);
        quote.shippingCost = applicableShippingMap.shipping ?: 0;
        quote.handlingCost = applicableShippingMap.handling ?: 0;
        quote.shippingTax = applicableShippingMap.tax ?: 0;
        cartItems.each {
            QuoteItem item = new QuoteItem()
            item.itemId = it.object.id;
            item.itemType = it.object.type;
            item.quantity = it.quantity;
            item.price = it.unitPrice;
            item.tax = it.tax;
            item.discount = it.discount;
            item.isShippable = it.isShippable;
            item.isTaxable = it.isTaxable;
            item.variations = it.variations;
            CartObject cartObject = it.object
            Map requestedParams = cartObject instanceof ProductInCartBase ? cartObject.requestedParams.findAll {it.key != "action" && it.key != "controller"} : [:]
            requestedParams = HookManager.hook("quoteItemParam-${cartObject.type}", requestedParams, cartObject)
            if(requestedParams) {
                item.params = requestedParams as JSON;
            }
            item.quote = quote;
            quote.addToQuoteItems(item);

        }
        quote.billing.save()
        quote.shipping.save()
        quote.save();
        if(!quote.hasErrors()) {
            sendEmail(quote)
            return true
        }
        return false
    }

    void sendEmail(Quote quote) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("get-quote")
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "quote_details":
                    Map quoteDetails = [:]
                    quoteDetails.items = [];
                    String name
                    quote.quoteItems.each {
                        Map item = [:];
                        item.product_name = it.cartItem.object.name
                        item.variations = it.variations ? it.variations.join(", ").encodeAsBMHTML() : "";
                        item.price = it.price.toPrice();
                        item.quantity = it.quantity;
                        item.discount = it.discount.toPrice();
                        item.tax = it.tax.toPrice();
                        item.total_with_tax_with_discount = it.getTotalAmount().toPrice()
                        quoteDetails.items.add(item);
                    }
                    quoteDetails.sub_total = quote.subTotal.toPrice();
                    quoteDetails.total_discount = quote.totalDiscount.toPrice();
                    quoteDetails.total_tax = quote.totalTax.toPrice();
                    quoteDetails.total_shipping_cost = quote.shippingCost.toPrice();
                    quoteDetails.shipping_tax = quote.shippingTax.toPrice();
                    quoteDetails.handling_cost = quote.handlingCost.toPrice();
                    quoteDetails.total = quote.getGrandTotal().toPrice();
                    refinedMacros[it.key] = quoteDetails;
                    break
                case "billing_address":
                    refinedMacros[it.key] = commonService.addressToMap(quote.billing);
                    break;
                case "shipping_address":
                    refinedMacros[it.key] = commonService.addressToMap(quote.shipping)
                    break;
                case "quote_id":
                    refinedMacros[it.key] = quote.id;
                    break;
                case "quote_date":
                    refinedMacros[it.key] = quote.created.toEmailFormat();
                    break;
                case "customer_name":
                    refinedMacros[it.key] = quote.customer.fullName();
                    break;
                case "currency_symbol":
                    refinedMacros[it.key] = AppUtil.baseCurrency.symbol;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, quote.billing.email)
    }

    Boolean removeQuote(Map params) {
        Quote quote = Quote.get(params.id);
        return removeQuote(quote)
    }

    Boolean removeQuote(Quote quote) {
        AppEventManager.fire("before-quote-delete", [quote.id])
        quote.delete()
        quote.billing.delete()
        quote.shipping.delete()
        AppEventManager.fire("quote-delete", [quote.id])
        return !quote.hasErrors()
    }

    Boolean requote(GrailsParameterMap params) {
        Closure sanitize = {Map map->
            Map response = [:]
            map.each {
                if(!it.key.contains(".")) {
                    response[it.key] = it.value
                }
            }
            return response;
        }
        Quote quote = Quote.get(params.quote)
        quote.shippingCost = params.double("shippingCost") ?: 0.0
        quote.handlingCost = params.double("handlingCost") ?: 0.0
        quote.shippingTax = params.double("shippingTax") ?: 0.0
        Map items = sanitize(params.items)
        params.list("removed").each {
            QuoteItem item = QuoteItem.get(it);
            item.delete()
        }
        items.each {
            QuoteItem item = QuoteItem.get(it.key);
            item.price = it.value.price.toDouble()
            item.tax = it.value.tax ? it.value.tax.toDouble(): 0.0
            item.discount = it.value.discount ? it.value.discount.toDouble() : 0.0
            item.quantity = it.value.quantity.toInteger()
            item.save()
        }
        if(params.shipping) {
            Address old = quote.shipping
            quote.shipping = orderService.getAddressFromJson(params.shipping)
            quote.shipping.save();
            old.delete();
        }
        if(params.billing) {
            Address old = quote.billing
            quote.billing = orderService.getAddressFromJson(params.billing)
            quote.billing.save();
            old.delete()
        }
        quote.save()
        return !quote.hasErrors()
    }

    CartItem quoteItemToCartItem(QuoteItem item) {
        CartItem cartItem = new CartItem(item.object, item.quantity, false);
        cartItem.unitTax = item.tax / item.quantity;
        cartItem.tax = item.tax
        cartItem.unitPrice = item.price
        cartItem.discount = item.discount
        cartItem.variations = (item.variations ? new ArrayList(item.variations) : [])
        return cartItem
    }

    Cart quoteToCart(Quote quote) {
        Cart cart = new Cart();
        cart.shippingCost = [0: [shipping: quote.shippingCost, handling: quote.handlingCost, tax: quote.shippingTax]];
        quote.quoteItems.each {
            CartItem item = quoteItemToCartItem(it)
            cart.cartItemList.add(item)
        }
        return cart
    }

    Boolean makeOrderFromQuote(Long id) {
        Quote quote = Quote.get(id)
        Cart cart = quoteToCart(quote)
        if(cart.cartItemList.size() != 0){
            Long orderId = orderService.saveOrder(cart, quote.billing, quote.shipping, quote.customer)
            cart.orderId = orderId
            AppEventManager.fire("order-confirm", [cart])
            removeQuote(quote)
            Thread.start {
                Thread.sleep(5000)
                AppUtil.initialDummyRequest()
                orderService.sendEmailForOrder(orderId, "create-order")
            }
            return true
        } else{
            throw new ApplicationRuntimeException("order.items.not.found")
        }
    }
}

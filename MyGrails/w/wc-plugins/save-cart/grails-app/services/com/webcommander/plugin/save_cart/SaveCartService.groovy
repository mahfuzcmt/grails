package com.webcommander.plugin.save_cart

import com.webcommander.admin.Customer
import com.webcommander.annotations.Initializable
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.CartObject
import com.webcommander.throwables.ApplicationRuntimeException
import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Initializable
@Transactional
class SaveCartService {

    static void initialize() {
        AppEventManager.on("before-customer-delete", { id ->
            List<SavedCart> carts = SavedCart.createCriteria().list {
                eq("customer.id", id)
            }
            carts.each {
                AppEventManager.fire("before-save-cart-delete", [it.id])
                it.delete()
            }
        })

        AppEventManager.on("before-save-cart-delete", { id ->
            SavedCart cart = SavedCart.proxy(id)
            cart?.cartItems.each {
                it.variations = []
            }*.delete()
        })
        AppEventManager.on("before-product-put-in-trash", {id, at2_reply ->
            SavedCartItem.createCriteria().list {
                eq("itemId", id)
                eq("itemType", NamedConstants.CART_OBJECT_TYPES.PRODUCT )
            }*.delete()
        })

        AppEventManager.on("before-simplified-event-delete", {id ->
            SavedCartItem.createCriteria().list {
                eq("itemId", id)
                eq("itemType", NamedConstants.CART_OBJECT_TYPES.SIMPLIFIED_EVENT_TICKET )
            }*.delete()
        })

        AppEventManager.on("before-general-event-delete", {id ->
            SavedCartItem.createCriteria().list {
                eq("itemId", id)
                eq("itemType", NamedConstants.CART_OBJECT_TYPES.GENERAL_EVENT_TICKET )
            }*.delete()
        })
    }

    List<SavedCart> getCarts(Long customerId) {
        return SavedCart.createCriteria().list {
            eq("customer.id", customerId)
        }
    }

    Integer getCartCount(Long customerId) {
        return SavedCart.createCriteria().count {
            eq("customer.id", customerId)
        }
    }

    Boolean save(Map params, Cart cart, Customer customer) {
        List<CartItem> cartItem = cart?.cartItemList
        if(!cartItem) {
            return false
        }
        Boolean isExist = SavedCart.createCriteria().list {
            eq("name", params.name)
            eq("customer.id", customer.id)
        } ? true : false;
        if(isExist) {
            throw new ApplicationRuntimeException("provided.field.value.exists.in.trash", ["name", params.name])
        }
        SavedCart savedCart = new SavedCart()
        savedCart.name = params.name
        savedCart.customer = customer
        cartItem.each {
            CartObject cartObject = it.object
            SavedCartItem item = new SavedCartItem(
                    itemType: cartObject.type,
                    itemId: cartObject.id,
                    quantity: it.quantity,
                    variations: it.variations
            )
            def requestedParams = cartObject.hasProperty("requestedParams") && cartObject.requestedParams ? cartObject.requestedParams.findAll {it.key != "action" && it.key != "controller"} : [:]
            requestedParams = HookManager.hook("savedCartItemParam-${cartObject.type}", requestedParams, cartObject)
            if(requestedParams) {
                item.params = requestedParams as JSON;
            }
            savedCart.addToCartItems(item)
        }
        savedCart.save();
        return !savedCart.hasErrors()
    }

    Boolean removeSavedCart(Map params) {
        SavedCart cart = SavedCart.get(params.cartId);
        AppEventManager.fire("before-saved-cart-delete", [cart.id])
        cart.delete()
        AppEventManager.fire("saved-cart-delete", [cart.id])
        return !cart.hasErrors()
    }
}

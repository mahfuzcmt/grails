package com.webcommander.plugin.gift_card.model

import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.util.AppUtil

class CacheOfGiftCardInCart {

    private static List<GiftCardInCart> getGiftCardsInCart() {
        List<GiftCardInCart> giftCardInCartList = CacheManager.get(NamedConstants.CACHE.TENANT_STATIC,"gift_cards_in_cart") as List<GiftCardInCart>
        if(giftCardInCartList == null) {
            setGiftCardsInCart(giftCardInCartList = [])
        }
        return giftCardInCartList
    }

    private static void setGiftCardsInCart(List<GiftCardInCart> giftCardInCarts) {
        CacheManager.cache(NamedConstants.CACHE.TENANT_STATIC, giftCardInCarts, -1, "gift_cards_in_cart")
    }

    private static class GiftCardInCart {
        Cart cart
        GiftCard giftCard
        Double availableAmount = 0.0
    }

    static List<GiftCardInCart> getAll() {
        return giftCardsInCart
    }

    static List<GiftCard> getAllGiftCard(Cart cart = null) {
        List<GiftCardInCart> giftCardInCarts = getAllGiftCardInCart(cart)
        if(giftCardInCarts) {
            return giftCardInCarts.giftCard
        }
        return []
    }

    static Double getAvailableAmount(Cart cart = null, GiftCard giftCard = null) {
        List<GiftCardInCart> giftCardInCarts = getAllGiftCardInCart(cart)
        if(giftCardInCarts) {
            if(giftCard) {
                GiftCardInCart giftCardInCart = giftCardInCarts.find { it.giftCard == giftCard }
                if(giftCardInCart) {
                    return giftCardInCart.availableAmount
                }
            } else {
                return giftCardInCarts.sum { it.availableAmount }
            }
        }
        return 0.0
    }

    static void add(Map model) {
        removeAll(model.cart, model.giftCard)
        giftCardsInCart.add(new GiftCardInCart(model))
    }

    static GiftCard find(Cart cart, GiftCard card) {
        return giftCardsInCart.find { it.cart == cart && it.giftCard == card}?.giftCard
    }

    static void removeAll(Cart cart, GiftCard giftCard) {
        giftCardsInCart.removeAll { it.cart == cart; it.giftCard == giftCard; }
    }

    private static List<GiftCardInCart> getAllGiftCardInCart(Cart cart = null) {
        if(!cart) {
            cart = CartManager.getCart(AppUtil.session.id)
        }
        return giftCardsInCart.findAll { it.cart == cart }
    }

    static {
        AppEventManager.on("cart-cleared cart-removed", { cart ->
            List<GiftCardInCart> giftCardInCarts = giftCardsInCart.findAll { it.cart == cart }
            giftCardsInCart.removeAll(giftCardInCarts)
        })
    }
}

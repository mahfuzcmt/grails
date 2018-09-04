package com.webcommander.plugin.gift_wrapper.site

import com.webcommander.authentication.annotations.License
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.gift_wrapper.GiftWrapper
import com.webcommander.plugin.gift_wrapper.GiftWrapperResourceTagLib
import com.webcommander.plugin.gift_wrapper.GiftWrapperService
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class GiftWrapperPageController {

    CommonService commonService
    GiftWrapperService giftWrapperService
    ProductService productService

    @License(required = "allow_gift_wrapper_feature")
    def giftWrapperPopup() {
        params.max = params.max ?: "10"
        String cartItemId = params.cartItemId
        String productId = params.productId
        Integer count = giftWrapperService.getGiftWrappersCount(params)
        List<GiftWrapper> giftWrappers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            giftWrapperService.getGiftWrappersForCustomer(params)
        }
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        def taxConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX)
        Cart cart = CartManager.getCart(session.id)
        CartItem cartItem = cart.cartItemList.find {
            it.id == Integer.parseInt(cartItemId)
        }
        def html = g.include(view: "/plugins/gift_wrapper/site/giftWrapperPopup.gsp", model: [success : true, cartItemId: cartItemId, cartItem: cartItem, productId: productId,
                                                                                              giftWrappers: giftWrappers, count: count, cartItem: cartItem, config: config, taxConfig: taxConfig, productData: cartItem.object.product])
        render([status: "success", html: html.toString()] as JSON)
    }

    @License(required = "allow_gift_wrapper_feature")
    def addGiftWrapperToCart() {
        Boolean success
        Long giftWrapperId = params.long("giftWrapperId")
        GiftWrapper giftWrapper = GiftWrapper.get(giftWrapperId)
        Integer cartItemId =  params.int("cartItemId")
        String giftWrapperMsg = params.giftWrapperMsg
        try {
            CartManager.addAmountWithCartItemUnitPrice(GiftWrapperResourceTagLib.GIFT_WRAPPER, giftWrapper.actualPrice, cartItemId, ["giftWrapper":giftWrapper, "message":giftWrapperMsg])
            success = true
        } catch(CartManagerException ex) {
            success = false
        }
        render([status: success] as JSON)
    }

    @License(required = "allow_gift_wrapper_feature")
    def removeGiftWrapperFromCartItem() {
        Boolean success
        Integer cartItemId = params.int("cartItemId")
        try {
            CartManager.removeAddedAmountFromCartItemUnitPrice(GiftWrapperResourceTagLib.GIFT_WRAPPER, cartItemId)
            success = true
        } catch(CartManagerException ex) {
            success = false
        }
        render([status:  success] as JSON)
    }


}

package com.webcommander.plugin.gift_wrapper

import com.webcommander.admin.MessageSource
import com.webcommander.calculator.TaxCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.models.blueprints.CartItemable
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import com.webcommander.webcommerce.TaxProfile
import grails.util.Holders

class BootStrap {

    GiftWrapperService giftWrapperService

    Map siteMessage = [
            "gift.wrapper.name" : "Gift Wrapper Name",
            "gift.wrapper.price": "Price",
            "gift.wrapping"     : "Gift Wrapping"
    ]

    def tenantInit = { tenant ->

        HookManager.register("resolveCartObject-product", { object, CartItemable cartItem ->
            println("Called from Gift Wrapper")
        })

        AppEventManager.on("after-order-save", { def orderItem, def cartItem, String type ->
            giftWrapperService.saveGiftWrapperData(orderItem, cartItem, type)
        })

        HookManager.register("getGiftWrapperPrice get-accessories-price", { def item ->
            def type
            if (item.hasProperty("savedCart")) {
                type = "saveCart"
            }
            GiftWrapperAssoc giftWrapperData = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(item.id, item.itemId, type, item.savedCartId)
            Double giftWrapperPrice = 0
            if (giftWrapperData) {
                GiftWrapper giftWrapper = GiftWrapper.get(giftWrapperData.giftWrapperId)
                if (giftWrapper) {
                    giftWrapperPrice = giftWrapper ? giftWrapper.price : giftWrapperPrice
                }
            }
            return giftWrapperPrice
        })

        HookManager.register("save-gift-wrapper-data after-quote-save after-saveCart-save after-order-save", {
            def orderItem, def cartItem, String type ->
                giftWrapperService.saveGiftWrapperData(orderItem, cartItem, type)
        })

        HookManager.register("get-gift-wrapper-display-price", { def item, def type, typeId ->
            GiftWrapperAssoc giftWrapperData = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(item.id, item.itemId, type, typeId)
            def giftWrapperPrice = ""
            if (giftWrapperData) {
                giftWrapperPrice = giftWrapperData.price
            }
            return giftWrapperPrice
        })

        HookManager.register("order-item-email-template-macro", { def item, def type, def order ->
            GiftWrapperAssoc giftWrapperAssoc = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(order.id, order.itemId, type, order.orderId)
            if (giftWrapperAssoc) {
                Map macros = [
                        gift_wrapper_name  : giftWrapperAssoc.giftWrapperName,
                        gift_wrapper_price : giftWrapperAssoc.price,
                        gift_wrapper_message: giftWrapperAssoc.message
                ]
                item += macros
            }
            return item
        })

        siteMessage.each {
            if (!MessageSource.findByMessageKeyAndLocale(it.key, "all")) {
                new MessageSource([messageKey: it.key, message: it.value, locale: "all"]).save()
            }
        }

    }

    def tenantDestroy = { tenant ->
        PluginDestroyUtil destroyUtil = new PluginDestroyUtil()
        try {
            destroyUtil.removeSiteMessage(*siteMessage.keySet())

        } catch (Exception e) {
            log.error "Could Not Deactivate Plugin giftwrapper From Tenant $tenant", e
            throw e
        } finally {
            util.closeConnection()
        }
    }

    def init = { servletContext ->
        TenantContext.eachParallelWithWait(tenantInit)
    }

}

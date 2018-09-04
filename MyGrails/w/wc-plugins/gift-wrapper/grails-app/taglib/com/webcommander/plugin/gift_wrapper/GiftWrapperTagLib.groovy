package com.webcommander.plugin.gift_wrapper

import com.webcommander.beans.SiteMessageSource
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.OrderItem

class GiftWrapperTagLib {

    static namespace = "giftWrapperTL"

    GiftWrapperService giftWrapperService
    SiteMessageSource siteMessageSource

    def giftWrapperPluginsJS = { Map attrs, body ->
        out << body()
        if ((!LicenseManager.isProvisionActive() || LicenseManager.license('allow_gift_wrapper_feature'))) {
            app.enqueueSiteJs(src: "plugins/gift-wrapper/js/site/gift-wrapper.js", scriptId: "gift-wrapper-admin")
        }
    }

    def adminJSs = { attr, body ->
        out << body()
        out << app.javascript(src: 'plugins/gift-wrapper/js/admin/gift-wrapper.js')
    }

    def giftWrapperView = { Map attrs, body ->
        if(GiftWrapper.count() > 0 && attrs.cartItem.object.product.productType == DomainConstants.PRODUCT_TYPE.PHYSICAL) {
            if (attrs.cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER)) {
                def giftWrapperPrice
                Map cartPageConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
                def giftWrapper = attrs.cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).giftWrapper
                if (cartPageConfig.price_enter_with_tax == "true") {
                    giftWrapperPrice = giftWrapperService.getGiftWrapperPriceWithTax(giftWrapper.actualPrice, attrs.cartItem)
                } else {
                    giftWrapperPrice = giftWrapper.actualPrice
                }
                out << "<div class='gift-wrapper'>" +
                        "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapper.name}</p>" +
                        "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperPrice.toCurrency().toPrice()} </p>" +
                        "<p>" +
                        "<span class='gift-wrapper-change-btn gift-wrapper-add-btn' product-id='${attrs.object.id}' item-id='${attrs.cartItem.id}'>${g.message(code: "change")}</span>" +
                        "<span>&nbsp;&nbsp;</span>" +
                        "<span class='gift-wrapper-remove-btn' product-id='${attrs.object.id}' gift-wrapper-id='${giftWrapper.id}' item-id='${attrs.cartItem.id}'>${g.message(code: "remove")}</span>" +
                        "</p>" +
                        "</div>"
            } else {
                out << "<div class='gift-wrapper'>" +
                        "<p>${siteMessageSource.convert("s:gift.wrapping")}  <span class='gift-wrapper-add-btn' item-id='${attrs.cartItem.id}'>${g.message(code: "add")}</span> </p>" +
                        "</div>"
            }
        }
    }


    def giftWrapperViewForOrderConfirmation = { Map attrs, body ->
        def giftWrapperPrice
        if (attrs.cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER)) {
            def giftWrapper = attrs.cartItem.getAdjustWithUnitPriceLogObject(GiftWrapperResourceTagLib.GIFT_WRAPPER).giftWrapper
            if (attrs.configs.price_enter_with_tax == "true") {
                giftWrapperPrice = giftWrapperService.getGiftWrapperPriceWithTax(giftWrapper.actualPrice, attrs.cartItem)
            } else {
                giftWrapperPrice = giftWrapper.actualPrice
            }
            out << "<div class='gift-wrapper'>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapper.name}</p>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperPrice.toCurrency().toPrice()} </p>" +
                    "</div>"
        }
    }

    def giftWrapperViewForOrdered = { Map attrs, body ->
        OrderItem orderItem = attrs.cartItem
        GiftWrapperAssoc giftWrapperAssoc = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(orderItem.id, orderItem.productId, "order", orderItem.orderId)
        if (giftWrapperAssoc) {
                out << "<div class='gift-wrapper'>" +
                        "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapperAssoc.giftWrapperName}</p>" +
                        "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperAssoc.price.toCurrency().toPrice()} </p>" +
                        "</div>"
        }
    }

    def giftWrapperViewForSaveCart = { Map attrs, body ->
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        def savedCartItem = attrs.cartItem
        GiftWrapperAssoc giftWrapperData = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(savedCartItem.id, savedCartItem.itemId, "saveCart", savedCartItem.savedCartId)
        if (giftWrapperData) {
            GiftWrapper giftWrapper = GiftWrapper.get(giftWrapperData.giftWrapperId)
            def giftWrapperPrice
            if (config.price_enter_with_tax == "true") {
                giftWrapperPrice = giftWrapperService.getGiftWrapperPriceWithTax(giftWrapper.price, attrs.cartItem)
            } else {
                giftWrapperPrice = Double.parseDouble(giftWrapper.price)
            }
            out << "<div class='gift-wrapper'>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapper.name}</p>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperPrice.toCurrency().toPrice()} </p>" +
                    "</div>"
        }
    }

    def giftWrapperViewForQuote = { Map attrs, body ->
        def quoteItem = attrs.cartItem
        GiftWrapperAssoc giftWrapperData = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(quoteItem.id, quoteItem.itemId, "quote", quoteItem.quoteId)
        if (giftWrapperData) {
            GiftWrapper giftWrapper = GiftWrapper.get(giftWrapperData.giftWrapperId)
            def giftWrapperPrice = giftWrapper.price
            out << "<div class='gift-wrapper'>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")}  ${giftWrapper.name}</p>" +
                    "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")}  ${giftWrapperPrice.toCurrency().toPrice()} </p>" +
                    "</div>"
        }
    }

    def getGiftWrapperPrice = { Map attrs, body ->
        Double giftWrapperPrice
        Map cartPageConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CART_PAGE)
        if (cartPageConfig.price_enter_with_tax == "true") {
            giftWrapperPrice = giftWrapperService.getGiftWrapperPriceWithTax(attrs.price, attrs.cartItem)
        } else {
            giftWrapperPrice = attrs.price
        }
        out << giftWrapperPrice.toCurrency().toPrice()
    }

    def giftWrapperViewForOrderDetailsAdminView = { Map attrs, body ->
        OrderItem orderItem = attrs.orderItem
        GiftWrapperAssoc giftWrapperAssoc = GiftWrapperAssoc.findByAssocItemIdAndProductIdAndAssocTypeAndAssocTypeId(orderItem.id, orderItem.productId, "order", orderItem.orderId)
        if (giftWrapperAssoc) {
                if (giftWrapperAssoc.message && !giftWrapperAssoc.message.trim().isEmpty()) {
                    out << "<div class='gift-wrapper'>" +
                            "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapperAssoc.giftWrapperName}</p>" +
                            "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperAssoc.price} </p>" +
                            "<p class='gift-wrapper-info'><span class='gift-wrapper-message-btn' gift-wrapper-msg='${giftWrapperAssoc.message}' >${g.message(code: "message")}</span>" +
                            "</p>" +
                            "</div>"
                } else {
                    out << "<div class='gift-wrapper'>" +
                            "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.name")} ${giftWrapperAssoc.giftWrapperName}</p>" +
                            "<p class='gift-wrapper-info'>${siteMessageSource.convert("s:gift.wrapper.price")} ${giftWrapperAssoc.price} </p>" +
                            "</div>"

                }

        }
    }


}

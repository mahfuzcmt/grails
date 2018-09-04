package com.webcommander.plugin.gift_card

import com.webcommander.ApplicationTagLib
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.Cart
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.plugin.gift_card.model.CacheOfGiftCardInCart
import com.webcommander.plugin.gift_card.webcommerce.GiftCardUsage
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import org.grails.taglib.GroovyPageAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class GiftCardApplicationTagLib {

    static namespace = "giftCard"

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    GiftCardService giftCardService

    def adminJSs = { attr, body ->
        out << body()
        out << app.javascript(src: 'plugins/gift-card/js/admin/gift-card.js')
        out << app.javascript(src: 'plugins/gift-card/js/admin/purchase-details.js')
    }

    def codePanel = { attr, body ->
        out << body()
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD, "is_enabled") != "true") {
            return
        }
        out << "<div class='gift-card-code-panel'>"
        if(request.giftCodeRedeemStatusMsg) {
            out << "<span class='message-block ${request.giftCodeRedeemStatusMsgType}'>"
            out << request.giftCodeRedeemStatusMsg
            out << "</span>"
        }
        out << "<label>"
        out << g.message(code: "gift.card")
        out << "</label>"
        out << "<input type='text' name='giftCardCode'/>"
        out << "<a class='apply-gift-card button'>"
        out << g.message(code: "apply")
        out << "</a>"
        out << "</div>"
    }

    def paymentOption = {attr, body ->
        out << body()
        DefaultPaymentMetaData giftCardPayment = pageScope.defaultPayments.find { it.identifier == "giftCard" }
        Cart cart = CartManager.getCart(session.id)
        out << g.render(template: "/plugins/gift_card/checkout/paymentOption", model: [giftCardPayment: giftCardPayment, availableBalance: CacheOfGiftCardInCart.getAvailableAmount(cart)])
    }

    def giftCardRow = { GroovyPageAttributes attr, body ->
        out << body()
        if(!giftCardService.isGiftCardRedeem(attr, request, pageScope)) {
            return
        }
        Double redeemFromGiftCard = 0.0
        if(attr.page == "paymentSuccess") {
            redeemFromGiftCard = giftCardService.getRedeemAmount(pageScope.order)
            out << "<tr class='gift-card-redeem-amount-row'>"
            out << "<td class='total-label'>"
            out << "<div class='wrapper'><span>"
            out << g.message(code: "payment.from.gift.card")
            out << "</span></div>"
            out << "</td>"
            out << "<td class='price'>"
            out << "<div class='wrapper'><span>"
            out << AppUtil.baseCurrency.symbol
            out << redeemFromGiftCard.toPrice()
            out << "</span></div>"
            out << "</td>"
            out << "</tr>"
        }
    }

    def customerProfileGiftCard = { attrs, body ->
        out << body()
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD, "is_enabled") != "true" && (!LicenseManager.isProvisionActive() || LicenseManager.license("allow_gift_card_feature"))) {
            return
        }
        app.enqueueSiteJs(src: "plugins/gift-card/js/site-js/customer-profile-gift-card-ext.js", scriptId: "giftCardExtra")
        long customerId = AppUtil.loggedCustomer
        def cardUsageList = GiftCardUsage.createCriteria().list {
            order {
                eq "customerId", customerId
            }
        }
        out << g.render(template:  "/plugins/gift_card/site/tabBody", model: [cardUsageList: cardUsageList])
    }

    def customerProfilePluginsJS = { attrs, body ->
        out << body()
        app.enqueueSiteJs(src: "plugins/gift-card/js/site-js/customer-profile-gift-card-ext.js", scriptId: "giftCardExtra")
    }

    def customerProfileTabBody = { attrs, body ->
        out << body()
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GIFT_CARD, "is_enabled") != "true" && (!LicenseManager.isProvisionActive() || LicenseManager.license("allow_gift_card_feature"))) {
            return
        }
        out << '<div id="bmui-tab-gift-card"></div>'
    }

    def callForPriceBlock = { attrs, body ->
        if(attrs.product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << ""
        } else {
            out << body()
        }
    }

    def advanceNumberBlock = { attrs, body ->
        if(attrs.product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << ""
        } else {
            out << body()
        }
    }

    def productPropertiesBlock = { attrs, body ->
        if(attrs.product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << ""
        } else {
            out << body()
        }
    }

    def shippingProfileBlock = { attrs, body ->
        if(attrs.product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << ""
        } else {
            out << body()
        }
    }

    def addCartPopup = { attrs, body ->
        out << body()
        if(attrs.product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            def cardData = params.gift_card ?: [:]
            if(cardData.sendingType == "email" && cardData.firstName && cardData.email) {
                return
            } else if(cardData.sendingType == "post" && cardData.firstName && cardData.email && cardData.address && cardData.postCode) {
                return
            }
            out << g.include(view: "/plugins/gift_card/giftCardExtraFields.gsp", model: [cardData: params.gift_card])
        }
    }

    def viewAddressToolTip = { attrs, body ->
        Product product = Product.get(attrs.orderItem.productId)
        if(product?.productType == DomainConstants.PRODUCT_TYPE.GIFT_CARD) {
            out << "<span class='view-address' order-item-id='${attrs.orderItem.id}'>${g.message(code: 'recipient.details')}</span>"
        }
    }

    def removeCardToken = { attrs, body ->
        out << body().toString().replaceAll(", Card Token: .{8}|Card Token: .{8}, |Card Token: .{8}", "").replaceAll("\\(\\)", "")
    }
}

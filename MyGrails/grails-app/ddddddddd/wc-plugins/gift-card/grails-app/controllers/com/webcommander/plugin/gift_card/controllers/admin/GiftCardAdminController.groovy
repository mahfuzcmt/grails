package com.webcommander.plugin.gift_card.controllers.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.DomainConstants as BASE_DC
import com.webcommander.models.ProductData
import com.webcommander.plugin.gift_card.GiftCardService
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class GiftCardAdminController {

    GiftCardService giftCardService
    ProductService productService

    def loadSettings() {
        Map configs = AppUtil.getConfig(BASE_DC.SITE_CONFIG_TYPES.GIFT_CARD)
        render(view: "/plugins/gift_card/admin/configs", model: [
            type: BASE_DC.SITE_CONFIG_TYPES.GIFT_CARD,
            gateway: PaymentGateway.findByCode(BASE_DC.PAYMENT_GATEWAY_CODE.GIFT_CARD),
            configs: configs
        ])
    }


    def loadPurchaseDetails() {
        params.max = params.max ?: "10";
        Product product = Product.get(params.productId)
        Integer count = giftCardService.getGiftCardCount(params)
        List<GiftCard> giftCards = giftCardService.getGiftCards(params)
        render(view: "/plugins/gift_card/admin/purchaseDetails/appView", model: [product: product, giftCards: giftCards, count: count]);
    }

    def adjustAmountPopup() {
        render(view: "/plugins/gift_card/admin/purchaseDetails/adjustAmountPopup")
    }

    def adjustAmount() {
        if(giftCardService.adjustAmount(params)) {
            render([status: "success", message: g.message(code:  "amount.adjust.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code:  "amount.adjust.failure")] as JSON)
        }
    }

    def changeStatus() {
        if(giftCardService.changeStatus(params.long('id'))) {
            render([status: "success", message: g.message(code:  "status.change.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code:  "status.change.failure")] as JSON)
        }
    }

    def addressToolTip() {
        GiftCard giftCard = GiftCard.findByOrderItemId(params.orderItemId)
        if(!giftCard) {
            throw new ApplicationRuntimeException("details.not.found")
        }
        render(view: "/plugins/gift_card/admin/addressDetailsToolTip", model: [giftCard: giftCard])
    }

    def loadCartSelectionPopup() {
        Product product = Product.get(params.id)
        ProductData data = productService.getProductData(product, [:])
        def eCommerceConfigs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)
        render(view: "/plugins/gift_card/admin/cartSelectionPopup", model: [productData: data, product: product, config: eCommerceConfigs])
    }
}

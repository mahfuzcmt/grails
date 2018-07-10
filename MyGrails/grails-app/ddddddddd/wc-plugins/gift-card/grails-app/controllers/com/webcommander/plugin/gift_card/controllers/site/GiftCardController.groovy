package com.webcommander.plugin.gift_card.controllers.site

import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.gift_card.GiftCardService
import com.webcommander.plugin.gift_card.webcommerce.GiftCard
import com.webcommander.plugin.gift_card.webcommerce.GiftCardUsage
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil

class GiftCardController {
    GiftCardService giftCardService

    @RequiresCustomer
    def loadGiftCard() {
        long customerId = AppUtil.loggedCustomer
        def cardUsageList = GiftCardUsage.createCriteria().list {
            order {
                eq "customerId", customerId
            }
        }
        render(template: "/plugins/gift_card/site/tabBody", model: [cardUsageList: cardUsageList])
    }

    @RequiresCustomer
    def checkBalance() {
        if(!params.code) {
            throw new ApplicationRuntimeException("give.valid.gift.card.code")
        }
        GiftCard giftCard = GiftCard.findByCode(params.code?.trim())
        if(!giftCard) {
            throw new ApplicationRuntimeException("give.valid.gift.card.code")
        } else if(!giftCard.isActive) {
            throw new ApplicationRuntimeException("gift.card.code.not.activated")
        }
        def eCommerceConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE)
        String continueShoppingUrl = null
        if (eCommerceConfig.continue_shopping_target == "previous") {
            continueShoppingUrl = request.getHeader("referer");
            if(continueShoppingUrl && continueShoppingUrl.contains("cart")) {
                continueShoppingUrl = session.lastCartReferer ?: app.relativeBaseUrl() + AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'landing_page')
            } else if(!continueShoppingUrl) {
                continueShoppingUrl = app.relativeBaseUrl() + AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'landing_page')
            } else {
                session.lastCartReferer = continueShoppingUrl
            }
        } else if(eCommerceConfig.continue_shopping_target == "home") {
            continueShoppingUrl = app.relativeBaseUrl() + AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, 'landing_page')
        } else {
            continueShoppingUrl = app.relativeBaseUrl() + eCommerceConfig.continue_shopping_specified_target
        }

        render(view: "/plugins/gift_card/site/giftCardBalance", model: [giftCard: giftCard, eCommerceConfig: eCommerceConfig, continueShoppingUrl: continueShoppingUrl])
    }

    @RequiresCustomer
    def profileToolTip() {
        render(view: "/plugins/gift_card/site/giftCardToolTip")
    }
}

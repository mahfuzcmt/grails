package com.webcommander.plugin.gift_card.mixin_service

class ProductWidgetService {

    def renderGiftCardSellInfoWidget(Map attrs, Writer writer) {
        renderService.renderView("/plugins/gift_card/productWidget/giftCardExtraFields", [:], writer)
    }

    def renderGiftCardSellInfoWidgetForEditor(Map attrs, Writer writer) {
        renderService.renderView("/plugins/gift_card/productWidget/editor/giftCardExtraFields", [:], writer)
    }
}

package com.webcommander.plugin.gift_card

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants

class GiftCardGrailsPlugin extends WebCommanderPluginBase {

    def title = "Gift card"
    def author = "Amir Hossain"
    def authorEmail = "amir@bitmascot.com"
    def description = '''GiftCard Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/gift-card";
    {
        _plugin = new PluginMeta(identifier: "gift-card", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                adminJss                        : [taglib: "giftCard", callable: "adminJSs"],
                subTotalLeftPanel               : [taglib: "giftCard", callable: "codePanel"],
                afterOrderTotalRow              : [taglib: "giftCard", callable: "giftCardRow", license: "false"],
                loyaltyPointConvertSettings     : [taglib: "giftCard", callable: "loyaltyPointConvertSettings"],
                customerProfilePluginsJS        : [taglib: "giftCard", callable: "customerProfilePluginsJS"],
                customerProfileGiftCard         : [taglib: "giftCard", callable: "customerProfileGiftCard"],
                customerProfileTabBody          : [taglib: "giftCard", callable: "customerProfileTabBody"],
                callForPriceBlock               : [taglib: "giftCard", callable: "callForPriceBlock"],
                advanceNumberBlock              : [taglib: "giftCard", callable: "advanceNumberBlock"],
                shippingProfileBlock            : [taglib: "giftCard", callable: "shippingProfileBlock"],
                productPropertiesBlock          : [taglib: "giftCard", callable: "productPropertiesBlock"],
                addCartPopup                    : [taglib: "giftCard", callable: "addCartPopup"],
                orderShortDetailsVariation      : [taglib: "giftCard", callable: "removeCardToken"],
                orderShortDetailsProductNameRow : [taglib: "giftCard", callable: "viewAddressToolTip"],
                checkoutPaymentOption           : [taglib: "giftCard", callable: "paymentOption"],
                adminProductVariationSelectPopup: [taglib: "giftCard", callable: "addCartPopup"]
        ]
    }
}

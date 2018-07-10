package com.webcommander.plugin.discount

class NameConstants {

    static TYPE = [
        (Constants.TYPE.CUSTOMER): "customer",
        (Constants.TYPE.PROMOTE_PRODUCT): "promote.product",
        (Constants.TYPE.OFFER_INCENTIVE_AND_SELL_MORE): "offer.incentive.sell.more",
        (Constants.TYPE.OFFER_COUPON): "offer.coupon"
    ]

    static DISCOUNT_AMOUNT_TYPE = [
        (Constants.AMOUNT_TYPE.FLAT): '$',
        (Constants.AMOUNT_TYPE.PERCENT): '%'
    ]

    static MINIMUM_AMOUNT_ON = [
        (Constants.MINIMUM_AMOUNT_ON.EACH_ITEM): "each.individual.item",
        (Constants.MINIMUM_AMOUNT_ON.TOTAL): "total.item"
    ]

    static MINIMUM_QTY_ON = [
        (Constants.MINIMUM_QTY_ON.EACH_ITEM): "each.individual.item",
        (Constants.MINIMUM_QTY_ON.TOTAL): "total.item"
    ]

    static DISCOUNT_DETAILS_TYPE = [
        (Constants.DETAILS_TYPE.AMOUNT): "discount.on.amount",
        (Constants.DETAILS_TYPE.SHIPPING): "discount.on.shipping",
        (Constants.DETAILS_TYPE.PRODUCT): "discount.on.product"
    ]
}

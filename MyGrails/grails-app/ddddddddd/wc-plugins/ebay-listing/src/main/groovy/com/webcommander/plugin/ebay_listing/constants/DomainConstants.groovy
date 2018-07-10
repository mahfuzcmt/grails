package com.webcommander.plugin.ebay_listing.constants

class DomainConstants {

    static PRICING_TYPE = [
        FIXED: "fixed",
        AUCTION: "auction"
    ]

    static PRICING_PROFILE_TYPE = [
        PRODUCT_DEFAULT_PRICE: "product-default-price",
        PRODUCT_ADDITIONAL_PRICE: "product-additional-price",
        NEW_PRICE: "new-price"
    ]

    static POSTAGE_TYPE = [
        NO_POSTAGE: 0,
        FLAT: 1,
        FREIGHT: 2,
        CALCULATED: 3
    ]

    static SELL_TO_QUANTITY_TYPE = [
        JUST_ONE_ITEM: 'just_one_item',
        MORE_THEN_ONE_QUANTITY: 'more_then_one_quantity',
        AVAILABLE_STOCK: 'available_stock'
    ]

    static PAYMENT_METHODS = [
        PERSONAL_CHECK: "PersonalCheck",
        CHASH_ON_DELIVERY: "COD",
        ESCROW: "Escrow",
        PAYMATE: "Paymate",
        PRO_PAY: 'ProPay',
        MONEY_BOOKERS: "Moneybookers"
    ]

    static SCHEDULE_BY = [
        MONTH: "month",
        WEEK: "week"
    ]
}

package com.webcommander.plugin.ebay_listing.constants

class NamedConstants {
    static PRICING_TYPE = [
        (DomainConstants.PRICING_TYPE.FIXED): "fixed",
        (DomainConstants.PRICING_TYPE.AUCTION): "auction"
    ]

    static PRICING_PROFILE_TYPE = [
        (DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_DEFAULT_PRICE): "product.default.price",
        (DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_ADDITIONAL_PRICE): "product.additional.price",
        (DomainConstants.PRICING_PROFILE_TYPE.NEW_PRICE): "new.price"
    ]

    static PAYMENT_METHODS = [
        (DomainConstants.PAYMENT_METHODS.PERSONAL_CHECK): "personal.check",
        (DomainConstants.PAYMENT_METHODS.CHASH_ON_DELIVERY): "cash.on.deliver",
        (DomainConstants.PAYMENT_METHODS.ESCROW): "escrow",
        (DomainConstants.PAYMENT_METHODS.PAYMATE): "paymate",
        (DomainConstants.PAYMENT_METHODS.MONEY_BOOKERS): "moneybookers",
        (DomainConstants.PAYMENT_METHODS.PRO_PAY): "pro.pay",
    ]

    static  SELL_TO_QUANTITY_TYPE = [
        (DomainConstants.SELL_TO_QUANTITY_TYPE.JUST_ONE_ITEM): 'just.one.item',
        (DomainConstants.SELL_TO_QUANTITY_TYPE.AVAILABLE_STOCK): 'available.stock',
        (DomainConstants.SELL_TO_QUANTITY_TYPE.MORE_THEN_ONE_QUANTITY): 'more.then.one.quantity'
    ]

    static DAYS = [0: 'sun', 1: 'mon', 2: 'tue', 3: 'wed', 4: 'thu', 5: 'fri', 6: 'sat']
    static MONTHS = [0: 'jan', 1: 'feb', 2: 'mar', 3: 'apr', 4: 'may', 5: 'jun', 6: 'jul', 7: 'aug', 8: 'sep', 9: 'oct', 10: 'nov', 11: 'dec']
    static SCHEDULE_BY = [
        (DomainConstants.SCHEDULE_BY.MONTH): "month",
        (DomainConstants.SCHEDULE_BY.WEEK): "week"
    ]
}

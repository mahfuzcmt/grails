package com.webcommander.plugin.discount

class Constants {

    static AMOUNT_TYPE = [
        FLAT: 'f',
        PERCENT: 'p'
    ]

    static TYPE = [
        CUSTOMER: "customer",
        PROMOTE_PRODUCT: "promote_product",
        OFFER_INCENTIVE_AND_SELL_MORE: "offer_incentive_and_sell_more",
        OFFER_COUPON: "offer_coupon"
    ]

    static CUSTOMER_DISCOUNT_CONDITION_TYPE = [ // Condition Type
        CERTAIN_CUSTOMER: "cc",
        CUSTOMER_PURCHASING_CERTAIN_PRODUCT: "cpcp",
        RETURNING_CUSTOMER: "rc",
        CUSTOMER_FROM_SPECIFIC_LOCATION: "cfsl"
    ]

    static PROMOTE_PRODUCT_DISCOUNT_CONDITION_TYPE = [
        CERTAIN_PRODUCT: "cp",
        CERTAIN_PRODUCT_PURCHASED_BY_CERTAIN_CUSTOMER: "cppcc",
        PRODUCT_WITH_CERTAIN_QUANTITY: "pcq",
    ]

    static OFFER_INCENTIVE_AND_SELL_MORE_DISCOUNT_CONDITION_TYPE = [
        DISCOUNT_ON_ORDER_AMOUNT: "doa",
        BUY_CERTAIN_PRODUCT_TO_GET_INCENTIVE_ON_CERTAIN_PRODUCT: "bcpicp"
    ]

    static OFFER_COUPON_DISCOUNT_CONDITION_TYPE = [
        CERTAIN_CUSTOMER: "occc",
        CERTAIN_PRODUCT: "occp",
        SHIPPING: "ocs",
        TRACK_REFERRALS: "octr"
    ]

    static CONDITION_TYPE = [
        (TYPE.CUSTOMER): CUSTOMER_DISCOUNT_CONDITION_TYPE,
        (TYPE.PROMOTE_PRODUCT): PROMOTE_PRODUCT_DISCOUNT_CONDITION_TYPE,
        (TYPE.OFFER_INCENTIVE_AND_SELL_MORE): OFFER_INCENTIVE_AND_SELL_MORE_DISCOUNT_CONDITION_TYPE,
        (TYPE.OFFER_COUPON): OFFER_COUPON_DISCOUNT_CONDITION_TYPE
    ]

    static DETAILS_TYPE = [
        AMOUNT: "amount",
        SHIPPING: "shipping",
        PRODUCT: "product",
    ]

    static AMOUNT_DETAILS_TYPE = [
        SINGLE: "single",
        TIERED: "tiered"
    ]

    static MINIMUM_AMOUNT_ON = [
        EACH_ITEM: "each_item",
        TOTAL: "total"
    ]

    static MINIMUM_QTY_ON = [
        EACH_ITEM: "each_item",
        TOTAL: "total"
    ]

    static PRODUCT_DETAILS_TYPE = [
        FREE_PRODUCT: "free_product",
        DISCOUNT_AMOUNT: "discount_amount",
        PRICE_CAP: "price_cap"
    ]

    static SHIPPING_DETAILS_TYPE = [
        FREE_SHIPPING: "free_shipping",
        SHIPPING_CAP: "shipping_cap",
        DISCOUNT_AMOUNT: "discount_amount"
    ]

    static SHIPPING_DETAILS_AMOUNT_TYPE = [
        SINGLE: "single",
        TIERED: "tiered"
    ]

    static PRODUCT_DETAILS_AMOUNT_TYPE = [
        SINGLE: "single",
        TIERED: "tiered"
    ]

    private static DISCOUNT_DETAILS_MAPPING = [
            (TYPE.OFFER_INCENTIVE_AND_SELL_MORE + '-' +CONDITION_TYPE[TYPE.OFFER_INCENTIVE_AND_SELL_MORE].BUY_CERTAIN_PRODUCT_TO_GET_INCENTIVE_ON_CERTAIN_PRODUCT) : [DETAILS_TYPE.PRODUCT],
            (TYPE.OFFER_INCENTIVE_AND_SELL_MORE + '-' +CONDITION_TYPE[TYPE.OFFER_INCENTIVE_AND_SELL_MORE].DISCOUNT_ON_ORDER_AMOUNT) : [DETAILS_TYPE.AMOUNT, DETAILS_TYPE.SHIPPING],
            (TYPE.OFFER_COUPON + '-' + CONDITION_TYPE[TYPE.OFFER_COUPON].CERTAIN_PRODUCT) : [DETAILS_TYPE.PRODUCT],
            (TYPE.OFFER_COUPON + '-' + CONDITION_TYPE[TYPE.OFFER_COUPON].SHIPPING) : [DETAILS_TYPE.SHIPPING]
    ]

    static List<String> getSupportedDiscountDetails(String type, conditionType) {
        return new ArrayList<String>(DISCOUNT_DETAILS_MAPPING[type + "-" + conditionType] ?: DETAILS_TYPE.values())
    }

    static DISCOUNT_PRIORITY_MATRIX = [
            FREE_SHIPPING           :       "free_shipping",
            FREE_PRODUCT            :       "free_product",
            SHIPPING_CAPPED_PRICE   :       "shipping_capped_price",
            PRODUCT_CAPPED_PRICE    :       "product_capped_price",
            SHIPPING_DISCOUNT_AMOUNT:       "shipping_discount_amount",
            PRODUCT_DISCOUNT_AMOUNT :       "product_discount_amount",
            ORDER_DISCOUNT_AMOUNT   :       "order_discount_amount",
    ]

    static DISCOUNT_AMOUNT_PRIORITY_MATRIX = [
            Constants.DISCOUNT_PRIORITY_MATRIX.SHIPPING_CAPPED_PRICE,
            Constants.DISCOUNT_PRIORITY_MATRIX.PRODUCT_CAPPED_PRICE,
            Constants.DISCOUNT_PRIORITY_MATRIX.SHIPPING_DISCOUNT_AMOUNT,
            Constants.DISCOUNT_PRIORITY_MATRIX.ORDER_DISCOUNT_AMOUNT
    ]

}

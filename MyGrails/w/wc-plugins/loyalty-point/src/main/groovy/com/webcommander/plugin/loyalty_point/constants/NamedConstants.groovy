package com.webcommander.plugin.loyalty_point.constants


/**
 * Created by sanjoy on 10/08/2014.
 */
class NamedConstants {
    static POINT_HISTORY_TYPE = [
            ORDER: "order",
            GIFT_CERTIFICATE: "point.to.gift.certificate",
            DISCOUNT_COUPON: "point.to.discount.coupon",
            STORE_CREDIT: "convert.to.store.credit",
            PRODUCT_REVIEW: "product.review",
            REGISTRATION: "registration",
            PURCHASE: "purchase",
            FB_SHARE: "facebook.share",
            TW_SHARE: "twitter.share",
            GP_SHARE: "googleplus.share",
            LN_SHARE: "linkedin.share",
            ON_SIGNUP_REFERRAL: "point.to.refer.signup",
            ON_SIGNUP_REFERRER: "point.to.be.referred.signup",
            ON_PURCHASE_REFERRAL: "point.to.refer.purchase",
            ON_PURCHASE_REFERREE: "point.to.be.referred.purchase"
    ]

    static RULE_TYPE = [
            (DomainConstants.RULE_TYPE.INCREASE) : "+",
            (DomainConstants.RULE_TYPE.MULTIPLY) : "x",
            STORE_CREDIT: "point.to.store.credit",
            ON_SIGNUP_REFERRAL: "point.to.refer.signup",
            ON_SIGNUP_REFERRER: "point.to.be.referred.signup",
            ON_PURCHASE_REFERRAL: "point.to.refer.purchase",
            ON_PURCHASE_REFERREE: "point.to.be.referred.purchase"
    ]
}

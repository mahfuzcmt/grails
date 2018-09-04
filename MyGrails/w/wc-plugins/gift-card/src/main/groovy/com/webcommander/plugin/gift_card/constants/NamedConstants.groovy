package com.webcommander.plugin.gift_card.constants

class NamedConstants {
    static GIFT_CARD_EXPIRY_THRESHOLD_UNITS = [
        (DomainConstants.GIFT_CARD_EXPIRY_THRESHOLD_UNITS.DAY): "days",
        (DomainConstants.GIFT_CARD_EXPIRY_THRESHOLD_UNITS.WEEK): "weeks",
        (DomainConstants.GIFT_CARD_EXPIRY_THRESHOLD_UNITS.MONTH): "months",
        (DomainConstants.GIFT_CARD_EXPIRY_THRESHOLD_UNITS.YEAR): "years"
    ]

    static APPLIED_GIFT_CARD_STATUS = [
        SUCCESSFULLY_APPLIED: "gift.card.successfully.applied",
        INVALID_CODE: "invalid.gift.code",
        INVALID_CARD: "invalid.gift.card",
        INACTIVE_CARD: "inactive.gift.card",
        ALREADY_REDEEMED: "already.redeemed",
        NO_BALANCE: "no.balance",
        NO_LONGER_AVAILABLE: "no.longer.available",
        NO_REDEEM_AMOUNT: "no.redeem.amount",
        NO_DUE_AMOUNT: "no.due.amount",
        MULTIPLE_TIMES_USAGE_NOT_ALLOWED: "multiple.times.usage.not.allowed"
    ]
}

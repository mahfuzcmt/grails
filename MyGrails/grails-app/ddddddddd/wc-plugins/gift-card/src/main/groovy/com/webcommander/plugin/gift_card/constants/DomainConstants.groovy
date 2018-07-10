package com.webcommander.plugin.gift_card.constants

class DomainConstants {
    static GIFT_CARD_EMAIL_TEMPLATES = [
        RECIPIENT_NOTIFICATION: "gift-card-recipient"
    ]

    static GIFT_CARD_EXPIRY_THRESHOLD_UNITS = [
        DAY: "day",
        WEEK: "week",
        MONTH: "month",
        YEAR: "year"
    ]

    static GIFT_CARD_SITE_CONFIGS = [
        is_enabled: "true",
        is_expiry_threshold_enabled: "0",
        expiry_threshold: "60",
        is_send_post_enabled: "1",
        expiry_threshold_unit: GIFT_CARD_EXPIRY_THRESHOLD_UNITS.DAY,
        tax_profile: "",
        discount_profile: "",
        loyalty_enable_conversion: "false",
        loyalty_conversion_rate: "0.5",
        gc_code_prefix: "GCRD-"
    ]
}

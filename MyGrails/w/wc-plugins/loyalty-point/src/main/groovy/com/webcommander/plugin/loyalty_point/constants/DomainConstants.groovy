package com.webcommander.plugin.loyalty_point.constants

/**
 * Created by sanjoy on 6/08/2014.
 */
class DomainConstants {
    static Double CONVERSION_CONSTANT_AMOUNT = 100;

    static POINT_POLICY = [
            ASSIGNED_TO_PRODUCTS: "assigned.to.products",
            HIGHEST_IN_PRODUCT_AND_CATEGORIES: "highest.in.product.and.categories",
            LOWEST_IN_PRODUCT_AND_CATEGORIES: "lowest.in.product.and.categories",
            HIGHEST_FROM_CATEGORIES: "highest.from.categories",
            LOWEST_FROM_CATEGORIES: "lowest.from.categories",
            FROM_PRIMARY_CATEGORY: "from.primary.category",
            SPECIFIED_CONVERSION_RATE: "specified.conversion.rate"
    ]

    static RULE_TYPE = [
            INCREASE: "point.increase",
            MULTIPLY: "increase.times"
    ]

    static ENABLE_EXPIRE = [
        NEVER: "never",
        AFTER_PERIOD: "after.period"
    ]

    static EXPIRE_IN_OFFSET = [
            DAYS: "days",
            MONTHS:"months",
            YEARS: "years"
    ]
}

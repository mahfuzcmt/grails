package com.webcommander.plugin.discount.resolver
/**
 * Created by sharif ul islam on 14/03/2018.
 */
class DiscountResolverFactory {

    private static final DiscountResolver DISCOUNT_RESOLVER = new DiscountResolver();
    private static final DiscountProfileResolver DISCOUNT_PROFILE_RESOLVER = new DiscountProfileResolver();
    private static final DiscountValueResolver DISCOUNT_VALUE_RESOLVER = new DiscountValueResolver();
    private static final DiscountOrderResolver DISCOUNT_ORDER_RESOLVER = new DiscountOrderResolver();

    static DiscountResolver getDiscountResolver () {
        return DISCOUNT_RESOLVER;
    }

    static DiscountProfileResolver getDiscountProfileResolver () {
        return DISCOUNT_PROFILE_RESOLVER;
    }

    static DiscountValueResolver getDiscountValueResolver () {
        return DISCOUNT_VALUE_RESOLVER;
    }

    static DiscountOrderResolver getDiscountOrderResolver () {
        return DISCOUNT_ORDER_RESOLVER;
    }

}

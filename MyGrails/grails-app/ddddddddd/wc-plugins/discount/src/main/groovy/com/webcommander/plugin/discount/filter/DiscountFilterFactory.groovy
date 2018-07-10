package com.webcommander.plugin.discount.filter
/**
 * Created by sharif ul islam on 28/03/2018.
 */
class DiscountFilterFactory {

    private static final DiscountDataFilter DISCOUNT_DATA_FILTER = new DiscountDataFilter();

    static DiscountDataFilter getDiscountDataFilter () {
        return DISCOUNT_DATA_FILTER;
    }

}

package com.webcommander.plugin.discount.processor
/**
 * Created by sharif ul islam on 28/03/2018.
 */
class ProcessorFactory {

    private static final DiscountProcessor DISCOUNT_PROCESSOR = new DiscountProcessor();

    static DiscountProcessor getDiscountProcessor () {
        return DISCOUNT_PROCESSOR;
    }

}

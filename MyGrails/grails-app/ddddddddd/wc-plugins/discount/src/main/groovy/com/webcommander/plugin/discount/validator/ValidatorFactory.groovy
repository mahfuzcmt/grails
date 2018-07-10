package com.webcommander.plugin.discount.validator
/**
 * Created by sharif ul islam on 14/03/2018.
 */
class ValidatorFactory {

    private static final DiscountProfileValidator DISCOUNT_PROFILE_VALIDATOR = new DiscountProfileValidator();
    private static final CouponCodeValidator COUPON_CODE_VALIDATOR = new CouponCodeValidator();

    static DiscountProfileValidator getDiscountProfileValidator () {
        return DISCOUNT_PROFILE_VALIDATOR;
    }

    static CouponCodeValidator getCouponCodeValidator () {
        return COUPON_CODE_VALIDATOR;
    }
}

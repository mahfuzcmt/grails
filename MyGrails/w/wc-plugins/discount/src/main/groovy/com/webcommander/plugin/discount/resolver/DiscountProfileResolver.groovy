package com.webcommander.plugin.discount.resolver

import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.plugin.discount.validator.Validator
import com.webcommander.plugin.discount.validator.ValidatorFactory
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.util.ResponseUtils

/**
 * Created by sharif ul islam on 12/03/2018.
 */
class DiscountProfileResolver extends Resolver {

    @Override
    Object doResolve(Map context) {

        Validator discountProfileValidator = ValidatorFactory.getDiscountProfileValidator()
        Validator couponCodeValidator = ValidatorFactory.getCouponCodeValidator()

        Map response = [:]
        List<CustomDiscount> resolvedProfiles = []

        List<CustomDiscount> generalProfiles = CustomDiscount.createCriteria().list {
            eq("isActive", true)

            if (context.productId) {

                and {
                    ne("discountDetailsType", Constants.DETAILS_TYPE.AMOUNT)
                    ne("discountDetailsType", Constants.DETAILS_TYPE.SHIPPING)
                }
            } else {
                or {
                    eq("discountDetailsType", Constants.DETAILS_TYPE.AMOUNT)
                    eq("discountDetailsType", Constants.DETAILS_TYPE.SHIPPING)
                }
            }

        }

        generalProfiles.each { profile ->
            Map validatorContext = [:]
            validatorContext.discount = profile
            validatorContext.productId = context.productId
            validatorContext.cart = context.cart
            Map validatorResponse = discountProfileValidator.validate(validatorContext)

            if (ResponseUtils.isSuccess(validatorResponse)) {

                if (profile.isApplyCouponCode) {
                    if (DiscountDataUtil.getEffectiveCouponCode()) {

                        Map couponValidatorContext = [:]
                        couponValidatorContext.couponCode = DiscountDataUtil.getEffectiveCouponCode()
                        couponValidatorContext.discount = profile
                        couponValidatorContext.cart = context.cart
                        Map couponValidatorResponse = couponCodeValidator.validate(couponValidatorContext)

                        if (ResponseUtils.isSuccess(couponValidatorResponse)) {
                            resolvedProfiles.add(profile)
                        }
                    }
                } else {
                    resolvedProfiles.add(profile)
                }

            }
        }

        response.resolvedProfiles = resolvedProfiles

        return response
    }

}

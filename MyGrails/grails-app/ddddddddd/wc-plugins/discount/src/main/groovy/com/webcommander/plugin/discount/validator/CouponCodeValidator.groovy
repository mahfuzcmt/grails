package com.webcommander.plugin.discount.validator

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.ResponseCodes
import com.webcommander.plugin.discount.CouponService
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCouponCode
import com.webcommander.util.AppUtil
import grails.util.Holders

/**
 * Created by sharif ul islam on 14/03/2018.
 */
class CouponCodeValidator implements Validator {

    private static CouponService _couponService

    private CouponService getCouponService() {
        return _couponService ?: (_couponService = Holders.applicationContext.getBean("couponService"))
    }

    boolean validate;

    @Override
    Map<String, Object> validate(Map<String, Object> context) {

        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> data = (Map<String, Object>) context.get("data");
        Map<String, Object> validationMessage = new HashMap<String, Object>();

        try {

            String couponCode = context.get("couponCode")
            CustomDiscount discount = context.get("discount")

            setValidate(true)
            String message = null

            if (!couponCode) {
                setValidate(false)
                message = "E300"
                validationMessage.put("couponCode", message)
            }

            if (!discount.isCreateUniqueCouponEachCustomer && !discount.coupon.code) {
                setValidate(false)
                message = "E300"
                validationMessage.put("couponCode", message)
            }

            if (!discount.isCreateUniqueCouponEachCustomer && discount.coupon.code) {
                if (!discount.coupon.code.isActive) {
                    setValidate(false)
                    message = "E301"
                    validationMessage.put("couponCode", message)
                }

                if (!discount.coupon.code.code.equals( couponCode )) {
                    setValidate(false)
                    message = "E302"
                    validationMessage.put("couponCode", message)
                }
            }

            if (discount.isCreateUniqueCouponEachCustomer) {
                DiscountCouponCode code = discount.coupon.assoc.codes.find{ it.code == couponCode }
                if (!code) {
                    setValidate(false)
                    message = "E300"
                    validationMessage.put("couponCode", message)
                } else {
                    if (!code.isActive) {
                        setValidate(false)
                        message = "E301"
                        validationMessage.put("couponCode", message)
                    }

                    if (!code.code.equals( couponCode )) {
                        setValidate(false)
                        message = "E302"
                        validationMessage.put("couponCode", message)
                    }

                    if (AppUtil.loggedCustomer) {
                        if (!code.customer) {
                            setValidate(false)
                            message = "E303"
                            validationMessage.put("couponCode", message)
                        } else {
                            if (!code.customer.id.equals(AppUtil.loggedCustomer)) {
                                setValidate(false)
                                message = "E303"
                                validationMessage.put("couponCode", message)
                            }
                        }
                    }
                }
            }

            if (!isValidate()) {

                response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.BAD_REQUEST);
                response.put(DomainConstants.RESPONSE_MESSAGE, "Coupon Code Validation Failed...!");

            } else {
                response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.SUCCESS_CODE);
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.put(DomainConstants.RESPONSE_CODE, ResponseCodes.INTERNAL_SERVER_ERROR_CODE);
            response.put(DomainConstants.RESPONSE_MESSAGE, "Coupon Code Validation Failed...!");

            return response;
        }

        response.put("data", data);
        response.put("validationMessage", validationMessage);

        return response;

    }

}

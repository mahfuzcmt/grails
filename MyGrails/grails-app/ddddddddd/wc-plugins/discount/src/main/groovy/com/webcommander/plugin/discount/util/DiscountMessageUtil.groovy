package com.webcommander.plugin.discount.util

import com.webcommander.ApplicationTagLib
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.util.AppUtil
import com.webcommander.util.ResponseUtils
import com.webcommander.util.TemplateMatcher

/**
 * Created by sharif ul islam on 05/04/2018.
 */
class DiscountMessageUtil {

    static ApplicationTagLib _app
    static ApplicationTagLib getApp() {
        return _app ?: (_app = AppUtil.getBean(ApplicationTagLib))
    }

    static TemplateMatcher engine = new TemplateMatcher("%", "%")

    static String getCouponStatusMessage(DiscountData discountData, String statusType) {

        String couponStatusMsg

        switch (statusType) {
            case ResponseUtils.STATUS_TYPE_SUCCESS:
                String mesg = app.message(code: 'successfully.applied.coupon.code')
                couponStatusMsg = engine.replace(mesg, [coupon_code: DiscountDataUtil.getEffectiveCouponCode()])
                if (discountData.discount.isDisplayTextCoupon) {
                    couponStatusMsg += "</br>" + discountData.discount.displayTextCoupon
                }
                break
            case ResponseUtils.STATUS_TYPE_ERROR:
                String mesg = app.message(code: 'invalid.coupon.code')
                couponStatusMsg = engine.replace(mesg, [coupon_code: DiscountDataUtil.getEffectiveCouponCode()])
                break
        }

        return couponStatusMsg
    }

}

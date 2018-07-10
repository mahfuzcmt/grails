package com.webcommander.plugin.discount.controllers.admin.webcommerce

import com.webcommander.common.CommonService
import com.webcommander.plugin.discount.CouponService
import com.webcommander.plugin.discount.DiscountService
import com.webcommander.plugin.discount.webcommerce.DiscountUsage
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCoupon
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCouponCode
import com.webcommander.util.DomainUtil
import grails.converters.JSON
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

/**
 * Created by sharif ul islam on 14/03/2018.
 */
class CouponController {

    CommonService commonService
    CouponService couponService
    DiscountService discountService

    def loadCouponCodes() {

        List<DiscountCouponCode> codeList = []
        Integer count = 0
        if (params.couponId) {

            DiscountCoupon coupon = DiscountCoupon.get(params.couponId)

            params.assocId = coupon.assoc.id

            params.max = params.max ?: "10"
            params.offset = params.offset ?: "0"
            count = couponService.getCouponCodeCount(params)
            codeList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
                params.offset = offset;
                couponService.getCouponCodes(params)
            }

        }

        render(view: "/plugins/discount/admin/viewCouponCodes", model: [count: count, codeList: codeList])
    }

    def couponCodeHistory() {

        List<DiscountCouponCode> codeList = []
        Integer count = 0
        if (params.discountId) {

            CustomDiscount discount = CustomDiscount.get(params.discountId)

            /*if (!discount.isApplyCouponCode) {
                render([status: "error", message: g.message(code: "discount.profile.not.for.coupon", args: [discount.name])] as JSON)
                return
            }*/

            if (!discount.isCreateUniqueCouponEachCustomer) {
                if (discount.coupon) {
                    codeList.add(discount.coupon.code)
                    count = 1
                }
            } else {
                params.assocId = discount.coupon.assoc.id

                params.max = params.max ?: "10"
                params.offset = params.offset ?: "0"
                count = couponService.getCouponCodeCount(params)
                codeList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
                    params.offset = offset;
                    couponService.getCouponCodes(params)
                }
            }

            List<Map> codes = []
            codeList.each {

                Map code = DomainUtil.toMap(it)

                Integer usageCounter = discountService.getDiscountUsageCount(["appliedCouponCode": it.code]) ?: 0

                code.put("usageCounter", usageCounter)

                codes.add(code)
            }

            render(view: "/plugins/discount/admin/couponCodeHistory", model: [count: count, codeList: codes, coupon: discount.coupon, discount: discount])
        }

    }

    def enableCode() {
        if (couponService.enableCode(params)) {
            render([status: "success", message: g.message(code: "success.enable.coupon.code", args: [])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "failed.enable.coupon.code", args: [params.field, params.value])] as JSON)
        }
    }

    def disableCode() {
        if (couponService.disableCode(params)) {
            render([status: "success", message: g.message(code: "success.disable.coupon.code", args: [])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "failed.disable.coupon.code", args: [params.field, params.value])] as JSON)
        }
    }

    def loadCouponCodeUsages() {

        List<DiscountUsage> usageList = []
        Integer count = 0
        if (params.discountCouponCodeId) {

            DiscountCouponCode code = DiscountCouponCode.get(params.discountCouponCodeId)
            //DiscountCoupon coupon = code.assoc ? DiscountCoupon.findByAssoc(code.assoc) : DiscountCoupon.findByCode(code)

            params.appliedCouponCode = code.code

            params.max = params.max ?: "10"
            params.offset = params.offset ?: "0"
            count = discountService.getDiscountUsageCount(params)
            usageList = commonService.withOffset(params.max, params.offset, count){max, offset, _count ->
                params.offset = offset;
                discountService.getDiscountUsages(params)
            }

        }

        render(view: "/plugins/discount/admin/viewCouponCodeUsages", model: [count: count, usageList: usageList])
    }

    def exportCoupon() {

        List<DiscountCouponCode> codeList = []
        Integer count = 0
        if (params.couponId) {

            DiscountCoupon coupon = DiscountCoupon.get(params.couponId)

            params.assocId = coupon.assoc.id

            codeList = couponService.getCouponCodes(params)
            //count = couponService.getCouponCodeCount(params)

            CsvListWriter listWriter = null
            try {
                response.setHeader("Content-Type", "text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=coupons.csv")
                OutputStreamWriter writer = new OutputStreamWriter(response.outputStream)
                listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE)
                String[] fields = ["Name", "Coupon"]
                listWriter.writeHeader(fields)
                codeList.each { code ->
                    List<String> fieldValueList = []

                    fieldValueList.add( code?.customer?.firstName.encodeAsBMHTML() + (!code?.customer?.isCompany ? (" " + code?.customer?.lastName?.encodeAsBMHTML()) : "" ) )

                    fieldValueList.add(code.code)

                    listWriter.write(fieldValueList)
                }
            } finally {
                if( listWriter != null ) {
                    listWriter.close()
                }
            }

        }

    }

}

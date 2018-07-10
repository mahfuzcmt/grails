package com.webcommander.plugin.discount.controllers.admin.webcommerce

import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.CouponService
import com.webcommander.plugin.discount.DiscountService
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCouponCode
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON

class DiscountController {

    CommonService commonService
    DiscountService discountService
    CouponService couponService
    ProductService productService
    ZoneService zoneService

    @Restriction(permission = "brand.view.list")
    @License(required = "allow_discount_feature")
    def loadAppView() {
        render(view: "/plugins/discount/admin/appView");
    }

    def editor() {
        CustomDiscount discount = params.id ? CustomDiscount.get(params.id) : new CustomDiscount(type: params.type ?: Constants.TYPE.CUSTOMER, discountDetailsType: Constants.DETAILS_TYPE.AMOUNT, defaultCouponCode: couponService.generateCouponCode());

        boolean isProfileUsed = false
        if (discount.id && discountService.getDiscountUsageCount([discountId: discount.id])) {
            isProfileUsed = true
        }
        //isProfileUsed = true

        render(view: "/plugins/discount/admin/editor/editor", model: [discount: discount, isProfileUsed: isProfileUsed])
    }

    def leftPanel() {
        params.max = "-1"
        params.offset = "0"
        Integer count = discountService.getDiscountCount(params)
        List<CustomDiscount> discounts = discountService.getDiscounts(params)
        render(view: "/plugins/discount/admin/leftPanel", model: [discounts: discounts, count: count, selected: params.long("selected")]);
    }

    @License(required = "allow_discount_feature")
    def save() {
        String message = 'discount.save.failure'
        CustomDiscount discount = null
        try {

            if (params.isApplyCouponCode && params.id && (params.defaultCouponCode != params.generatedCouponCode) && !commonService.isUnique(DiscountCouponCode, [field: "code", value: params.defaultCouponCode])) {
                render([status: "error", message: g.message(code: "coupon.code.exists", args: [params.defaultCouponCode])] as JSON)
                return
            }

            discount = discountService.save(params)
            Map response = [success: false], data = [discountId: discount.id]
            HookManager.hook("discountSave", response, data)
        } catch (Exception ex) {
            log.error(ex.message, ex)
            if(ex.args[0].equals("offer_coupon")) message = ex.message
        }
        if(discount) {
            render([status: "success", message: g.message(code:  'discount.save.success')] as JSON)
        } else {
            render([status: "error", message: g.message(code:  message)] as JSON)
        }
    }

    def loadDiscountDetails() {

        List<Zone> zones = zoneService.getZones([
                //max: params.max ?: 5,
                //searchText: params.searchText,
                isDefault: false
        ])

        boolean isProfileUsed = false

        CustomDiscount discount = params.discountId ? CustomDiscount.get(params.discountId) : new CustomDiscount(discountDetailsType: Constants.DETAILS_TYPE.AMOUNT)

        if (discount.id && discountService.getDiscountUsageCount([discountId: discount.id])) {
            isProfileUsed = true
        }

        String type = params.type
        String conditionType = params.conditionType;
        List<String> supportedDetails = Constants.getSupportedDiscountDetails(type, conditionType);
        render(view: "/plugins/discount/admin/editor/discountDetails", model: [supportedDetails: supportedDetails, discount: discount, zones: zones, isProfileUsed: isProfileUsed])
    }

    def copy() {
        Long savedId = discountService.copyDiscount(params.id as Long)
        if(savedId) {
            render([status: "success", message: g.message(code: "discount.copy.success"), id: savedId] as JSON)
        } else {
            render([status: "error", message: g.message(code: "discount.copy.failure")] as JSON)
        }
    }

    def delete() {
        Boolean result = true
        try {
            discountService.delete(params.long("id"))
        } catch (Exception ex) {
            result = false
            log.error(ex.message, ex)
        }
        if(result) {
            render([status: "success", message: g.message(code:  'discount.remove.success')] as JSON)
        } else {
            render([status: "error", message: g.message(code:  'discount.remove.failure')] as JSON)
        }
    }

    def explorerView() {
        CustomDiscount discount = params.id ? CustomDiscount.get(params.id) : null
        render(view: "/plugins/discount/admin/explorerView", model: [discountItem: discount]);
    }

    def isUnique() {
        render(commonService.responseForUniqueField(CustomDiscount, params.long("id"), params.field, params.value) as JSON)
    }

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.DISCOUNT);
        render(view: "/plugins/discount/admin/config", model: [config: config]);
    }

    def isCouponUnique() {
        params.field = "code"
        if (commonService.isUnique(DiscountCouponCode, params)) {
            render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
        } else {
            render([status: "error", message: g.message(code: "coupon.code.exists", args: [params.value])] as JSON)
        }
    }

}

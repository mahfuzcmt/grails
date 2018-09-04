package com.webcommander.plugin.discount.webcommerce

import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.webcommerce.coupon.DiscountCoupon
import com.webcommander.plugin.discount.webcommerce.details.AmountDiscountDetails
import com.webcommander.plugin.discount.webcommerce.details.ProductDiscountDetails
import com.webcommander.plugin.discount.webcommerce.details.ShippingDiscountDetails
import com.webcommander.webcommerce.Product

/**
 * Created by sharif ul islam on 06/03/2018.
 */
class CustomDiscount {
    Long id

    String name
    String type
    String discountDetailsType
    String defaultCouponCode
    String displayTextCoupon
    String displayTextCart
    String displayTextPartialDiscountCondition

    Boolean isActive = true
    Boolean isSpecifyEndDate = false
    Boolean isExcludeProductsOnSale = false
    Boolean isDiscountUsedWithOtherDiscount = false
    Boolean isMaximumUseTotal = false
    Boolean isMaximumUseCustomer = false
    Boolean isMaximumDiscountAllowed = false
    Boolean isApplyCouponCode = false
    Boolean isDisplayTextCoupon = false
    Boolean isDisplayDiscountInformationProdDetail = true
    Boolean isDisplayTextCart = false
    Boolean isDisplayTextPartialDiscountCondition = false
    Boolean isCreateUniqueCouponEachCustomer = false
    Boolean isCouponCodeAutoGenerate = true

    Integer maximumUseCount
    Integer maximumUseCustomerCount

    Double maximumDiscountAllowedAmount

    Date startFrom
    Date startTo

    Long detailsId

    Date created
    Date updated

    DiscountAssoc assoc
    DiscountCoupon coupon

    Collection<DiscountUsage> usage = []
    Collection<Product> excludeProducts = []

    static hasMany = [usage: DiscountUsage]

    static clone_exclude = ["usage", "assoc", "coupon"]
    static copy_reference = ["excludeProducts"]

    static mapping = {
        excludeProducts cache: false
    }

    static constraints = {

        name(unique: true)

        type(nullable: true)

        defaultCouponCode(nullable: true)
        displayTextCoupon(nullable: true)
        displayTextCart(nullable: true)
        displayTextPartialDiscountCondition(nullable: true)

        maximumUseCount(nullable: true)
        maximumUseCustomerCount(nullable: true)
        maximumDiscountAllowedAmount(nullable: true)

        startFrom(nullable: true)
        startTo(nullable: true)

        coupon(nullable: true)

    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    def getDiscountDetails() {
        if(detailsId == null) { return null }
        if(this.discountDetailsType == Constants.DETAILS_TYPE.AMOUNT) {
            return AmountDiscountDetails.get(this.detailsId)
        } else if(this.discountDetailsType == Constants.DETAILS_TYPE.PRODUCT) {
            return ProductDiscountDetails.get(this.detailsId)
        } else {
            return ShippingDiscountDetails.get(this.detailsId)
        }
    }

    def initiateDiscountDetails(String detailsType) {
        if (this.detailsId && detailsType.equals(this.discountDetailsType)) {
            return getDiscountDetails()
        }
        if(detailsType == Constants.DETAILS_TYPE.AMOUNT) {
            return new AmountDiscountDetails()
        } else if(detailsType == Constants.DETAILS_TYPE.PRODUCT) {
            return new ProductDiscountDetails()
        } else if(detailsType == Constants.DETAILS_TYPE.SHIPPING) {
            return new ShippingDiscountDetails()
        }
        return null
    }

    def isCertainCustomerSelected() {
        return assoc && !assoc.isAppliedAllCustomer && (assoc.customers || assoc.customerGroups)
    }

    def isCertainProductSelected() {
        return assoc && !assoc.isAppliedAllProduct && (assoc.products || assoc.categories)
    }

}

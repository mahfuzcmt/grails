package com.webcommander.plugin.discount

import com.webcommander.admin.Customer
import com.webcommander.admin.CustomerGroup
import com.webcommander.admin.Zone
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.webcommerce.DiscountUsage
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.DiscountAssoc
import com.webcommander.plugin.discount.webcommerce.details.*
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ShippingClass
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap
import grails.web.databinding.DataBindingUtils
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Transactional
class DiscountService {

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    CommonService commonService
    CouponService couponService


    CustomDiscount save(TypeConvertingMap params) {
        def session = AppUtil.session
        CustomDiscount discount = params.id ? CustomDiscount.get(params.id) : new CustomDiscount(type: params.type);
        def checkBoxList = ["isActive", "isSpecifyEndDate", "isExcludeProductsOnSale", "isDiscountUsedWithOtherDiscount", "isMaximumUseTotal", "isMaximumUseCustomer", "isMaximumDiscountAllowed", "isDisplayTextCoupon", "isDisplayDiscountInformationProdDetail", "isDisplayTextCart", "isDisplayTextPartialDiscountCondition"]
        def couponCheckBoxList = ["isApplyCouponCode", "isCouponCodeAutoGenerate", "isCreateUniqueCouponEachCustomer"]
        if(!commonService.isUnique(discount, "name")) {
            throw new ApplicationRuntimeException("discount.name.exists")
        }
        DataBindingUtils.bindObjectToInstance(discount, params, null, ["id", "startFrom", "startTo"] + checkBoxList + couponCheckBoxList, null)

        discount.startFrom = params.startFrom ? params.startFrom?.toDate()?.gmt(session.timezone) : null
        discount.startTo = params.startTo ? params.startTo?.toDate()?.gmt(session.timezone) : null

        if (!discount.isSpecifyEndDate) {
            discount.startTo = null
        }

        for(String checkBoxItem : checkBoxList) {
            discount."$checkBoxItem" = params."$checkBoxItem" ? true : false
        }

        boolean isProfileUsed = false
        if (discount.id && getDiscountUsageCount([discountId: discount.id])) {
            isProfileUsed = true
        }

        if (!isProfileUsed) {
            for(String checkBoxItem : couponCheckBoxList) {
                discount."$checkBoxItem" = params."$checkBoxItem" ? true : false
            }
        }

        //discount.isActive = true

        discount.discountDetails?.delete()
        Object discountDetails = this."save${discount.discountDetailsType.camelCase(true)}DiscountDetails"(params)
        discount.detailsId = discountDetails.id

        if (!params.id) {
            discount.assoc = new DiscountAssoc()
        }

        prepareDiscountAssoc(discount.assoc, params)

        // Exclude Products
        if (params.excludeProducts) {
            discount.excludeProducts.clear()
            List products = params.list("excludeProducts")
            for(String productId : products) {
                discount.excludeProducts.add(Product.get(productId.toLong()))
            }
        }

        // if apply view coupon code
        if (discount.isApplyCouponCode && !isProfileUsed) {
            couponService.prepareDiscountCoupon(discount)
        }

        discount.save()

        AppEventManager.fire("discount-update")

        return discount
    }

    List<CustomDiscount> getDiscounts(Map params) {
        Map limitParams = [max: params.max ?: -1, offset: params.offset ?: 0]
        return CustomDiscount.createCriteria().list(limitParams) {
            and getCriteriaClosure(params)
            order(params.sort ?: "created", params.dir ?: "desc")
        }
    }

    Integer getDiscountCount(Map params) {
        return CustomDiscount.createCriteria().count({
            and getCriteriaClosure(params)
        })
    }

    Long copyDiscount(Long discountId) {
        CustomDiscount discount = CustomDiscount.get(discountId)

        CustomDiscount discountCopy = DomainUtil.clone(discount, ["name"])
        discountCopy.assoc = DomainUtil.clone(discount.assoc)
        discountCopy.name = commonService.getCopyNameForDomain(discount)

        def newDetails = DomainUtil.clone(discount.discountDetails)
        newDetails.save();
        discountCopy.detailsId = newDetails.id

        discountCopy.save()
        if(discount.hasErrors()) {
            return null
        }
        return discountCopy.id
    }

    void delete(Long id) {
        CustomDiscount discount = CustomDiscount.get(id)

        discount.discountDetails?.delete()

        discount.assoc.initiate()
        discount.save()

        discount.delete()
        AppEventManager.fire("discount-update")
    }

    void prepareDiscountAssoc(DiscountAssoc assoc, TypeConvertingMap params) {
        assoc.initiate()

        if (params.product) {
            List products = params.list("product")
            for(String productId : products) {
                assoc.products.add(Product.get(productId.toLong()))
            }
        }
        if (params.category) {
            List categories = params.list("category")
            for(String categoryId : categories) {
                assoc.categories.add(Category.get(categoryId.toLong()))
            }
        }

        if (params.customer) {
            List customers = params.list("customer")
            for(String customerId : customers) {
                assoc.customers.add(Customer.get(customerId.toLong()))
            }
        }
        if (params.customerGroup) {
            List customerGroups = params.list("customerGroup")
            for(String customerGroupId : customerGroups) {
                assoc.customerGroups.add(CustomerGroup.get(customerGroupId.toLong()))
            }
        }

        assoc.isAppliedAllCustomer = params.isAppliedAllCustomer ?: false
        assoc.isAppliedAllProduct = params.isAppliedAllProduct ?: false

    }

    AmountDiscountDetails saveAmountDiscountDetails(TypeConvertingMap params) {
        TypeConvertingMap detailsParams = params.amountDetails
        AmountDiscountDetails details =  new AmountDiscountDetails(type:  detailsParams.type);
        switch (details.type) {
            case Constants.AMOUNT_DETAILS_TYPE.SINGLE:
                details.singleAmount = detailsParams.double("singleAmount")
                details.singleAmountType = detailsParams.singleAmountType
                break
            case Constants.AMOUNT_DETAILS_TYPE.TIERED:
                detailsParams.amountTier.each {String key, value ->
                    if(key.contains(".")) {return}
                    DiscountAmountTier amountTier = new DiscountAmountTier()
                    amountTier.amount = value.double("amount")
                    amountTier.minimumAmount = value.double("minimumAmount")
                    amountTier.amountType = value.amountType
                    details.addToTiers(amountTier)
                }
                details.minimumAmountOn = detailsParams.minimumAmountOn
                break
        }
        details.save()
        return details
    }

    ShippingDiscountDetails saveShippingDiscountDetails(TypeConvertingMap params) {
        TypeConvertingMap detailsParams = params.shippingDetails
        ShippingDiscountDetails details =  new ShippingDiscountDetails(type:  detailsParams.type);
        details.zone = detailsParams.zone ? Zone.get(detailsParams.zone) : null
        details.shippingClass = detailsParams.shippingClass ? ShippingClass.get(detailsParams.shippingClass) : null
        switch (details.type) {
            case Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP:
                details.capAmount = detailsParams.double("capAmount")
                break
            case Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT:
                details.amountType = detailsParams.amountType
                switch (details.amountType) {
                    case Constants.SHIPPING_DETAILS_AMOUNT_TYPE.SINGLE:
                        details.singleAmount = detailsParams.double("singleAmount")
                        details.singleAmountType = detailsParams.singleAmountType
                        break
                    case Constants.SHIPPING_DETAILS_AMOUNT_TYPE.TIERED:
                        detailsParams.amountTier.each {String key, value ->
                            if(key.contains(".")) {return}
                            DiscountAmountTier amountTier = new DiscountAmountTier(details: details)
                            amountTier.amount = value.double("amount")
                            amountTier.minimumAmount = value.double("minimumAmount")
                            amountTier.amountType = value.amountType
                            details.addToTiers(amountTier)
                        }
                        break

                }
                break
        }
        details.save()
        return details
    }

    ProductDiscountDetails saveProductDiscountDetails(TypeConvertingMap params) {
        TypeConvertingMap detailsParams = params.productDetails
        ProductDiscountDetails details =  new ProductDiscountDetails(type:  detailsParams.type);
        List discountProducts = params.list("discountProducts")
        for(String productId : discountProducts) {
            details.productIds.add(Long.parseLong(productId))
        }
        switch (details.type) {
            case Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT:
                details.freeProductMaxQty = detailsParams.int("freeProductMaxQty")
                break
            case Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT:
                details.amountType = detailsParams.amountType
                switch (details.amountType) {
                    case Constants.PRODUCT_DETAILS_AMOUNT_TYPE.SINGLE:
                        details.singleAmount = detailsParams.double("singleAmount")
                        details.singleAmountType = detailsParams.singleAmountType
                        break
                    case Constants.PRODUCT_DETAILS_AMOUNT_TYPE.TIERED:
                        detailsParams.quantityTier.each {String key, value ->
                            if(key.contains(".")) {return}
                            DiscountQtyTier quantityTier = new DiscountQtyTier()
                            quantityTier.amount = value.double("amount")
                            quantityTier.minimumQty = value.double("minimumQty")
                            quantityTier.amountType = value.amountType
                            details.addToTiers(quantityTier)
                        }
                        details.minimumQtyOn = detailsParams.minimumQtyOn
                        break

                }
                break
            case Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP:
                details.capPriceMaxQty = detailsParams.int("capPriceMaxQty")
                details.capPrice = detailsParams.double("capPrice")
                break
        }
        details.save()
        return details
    }

    List<DiscountUsage> getDiscountUsages (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return DiscountUsage.createCriteria().list(listMap) {
            and getDiscountUsageCriteriaClosure(params);
            order(params.sort ?: "id", params.dir ?: "desc");
        }
    }

    Integer getDiscountUsageCount (Map params) {
        return DiscountUsage.createCriteria().get {
            and getDiscountUsageCriteriaClosure(params);
            projections {
                rowCount();
            }
        }
    }

    DiscountUsage saveDiscountUsage(Map context) {

        DiscountData data = context.data

        if (data) {
            DiscountUsage usage = new DiscountUsage()
            usage.discount = data.discount
            usage.orderId = context.orderId
            usage.amount = data.resolvedAmount ?: 0
            usage.itemId = context.itemId
            usage.customerId = context.customerId
            usage.appliedCouponCode = context.appliedCouponCode
            usage.save()
            return usage
        }
        return null
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if(params.type) {
                eq("type", params.type)
            }
        }
    }

    private Closure getDiscountUsageCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {

            if (params.discountId) {
                eq("discount.id", params.discountId)
            }

            if (params.orderId) {
                eq("orderId", params.orderId)
            }

            if (params.itemId) {
                eq("itemId", params.itemId)
            }

            if (params.customerId) {
                eq("customerId", params.customerId)
            }

            if (params.appliedCouponCode) {
                eq("appliedCouponCode", params.appliedCouponCode)
            }

            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
        }
        return closure;
    }

}

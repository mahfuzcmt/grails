package com.webcommander.plugin.discount.util

import com.webcommander.admin.Customer
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.details.ProductDiscountDetails
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product

/**
 * Created by sharif ul islam on 25/03/2018.
 */
class DiscountDataUtil {

    static List<Product> getAllProducts(CustomDiscount discount) {
        List<Product> products = []
        products.addAll(discount.assoc.products)

        discount.assoc.categories.each {
            products.addAll( it.products )
        }

        return products
    }

    static List<Product> getAllCustomers(CustomDiscount discount) {
        List<Customer> customers = []
        customers.addAll(discount.assoc.customers)

        discount.assoc.customerGroups.each {
            customers.addAll( it.customers )
        }

        return customers
    }

    static String getEffectiveCouponCode() {
        if (AppUtil.params.couponCode) {
            AppUtil.session.effectiveCouponCode = AppUtil.params.couponCode
        }
        return AppUtil.session.effectiveCouponCode
    }

    static clearDiscountData(Cart cart) {
        if (cart) {
            restoreCartItem(cart)
            cart.actualTax = 0
            cart.discountOnOrder = 0
            cart.discountOnShipping = 0
            cart.selectedDiscountData = null
            cart.cartItemList.each { CartItem item ->
                item.discountData = null
                item.discount = 0
                item.actualDiscount = 0
                item.taxDiscount = 0
            }
        }
    }

    static DiscountData getMaxAmountData(List<DiscountData> dataList) {
        Double amount = 0
        DiscountData filteredDiscountData
        for(DiscountData data : dataList) {
            if(data.resolvedAmount > amount) {
                amount = data.resolvedAmount
                filteredDiscountData = data
            }
        }
        return filteredDiscountData
    }

    /*static DiscountData getMaxProductPriceCapData(List<DiscountData> dataList) {
        Integer capPriceMaxQty = 0
        DiscountData filteredDiscountData
        for(DiscountData data : dataList) {
            ProductDiscountDetails details = data.discount.discountDetails
            if(details.capPriceMaxQty > capPriceMaxQty) {
                capPriceMaxQty = details.capPriceMaxQty
                filteredDiscountData = data
            }
        }
        return filteredDiscountData
    }*/

    static Map getMaxProductPriceCapData(List<DiscountData> dataList, List<DiscountData> allDataList) {
        Map response = [:]
        List<DiscountData> resolveDataList
        DiscountData resolveData
        Double amount = 0
        dataList.each {DiscountData data ->
            List<DiscountData> discountDataList = allDataList.findAll {
                data.discount.id == it.discount.id && it.isPriceCap
            }
            Double resolvedAmounts = discountDataList.sum{it.resolvedAmount}

            if (discountDataList && (data.discount.isMaximumDiscountAllowed && (resolvedAmounts > data.discount.maximumDiscountAllowedAmount))) {
                resolvedAmounts = data.discount.maximumDiscountAllowedAmount
                Integer resolvedQuantitys = discountDataList.sum{it.resolvedQuantity}
                Double unitDiscount = resolvedAmounts / resolvedQuantitys
                discountDataList.each {
                    it.resolvedAmount = unitDiscount * it.resolvedQuantity
                }
            }

            if(resolvedAmounts > amount) {
                amount = resolvedAmounts
                resolveDataList = discountDataList
                resolveData = data
            }
        }

        if (!resolveData && dataList) {
            resolveData = dataList.get(0)
        }

        response.resolveDataList = resolveDataList
        response.resolveData = resolveData

        return response
    }

    static Map getMaxProductAmountData(List<DiscountData> dataList, List<DiscountData> allDataList) {
        Map response = [:]
        List<DiscountData> resolveDataList
        DiscountData resolveData
        Double amount = 0
        dataList.each {DiscountData data ->
            List<DiscountData> discountDataList = allDataList.findAll {
                data.discount.id == it.discount.id && it.isDiscountOnProductAmount
            }

            Double resolvedAmounts = discountDataList.sum{it.resolvedAmount}
            if (data.discount.discountDetails.amountType == Constants.AMOUNT_DETAILS_TYPE.TIERED && data.discount.discountDetails.minimumQtyOn == Constants.MINIMUM_AMOUNT_ON.TOTAL && discountDataList) {
                resolvedAmounts = discountDataList.get(0).resolvedAmount
            }

            if (discountDataList && (data.discount.discountDetails.amountType == Constants.AMOUNT_DETAILS_TYPE.SINGLE || (data.discount.discountDetails.amountType == Constants.AMOUNT_DETAILS_TYPE.TIERED && data.discount.discountDetails.minimumQtyOn == Constants.MINIMUM_AMOUNT_ON.EACH_ITEM))
                && data.discount.isMaximumDiscountAllowed && (resolvedAmounts > data.discount.maximumDiscountAllowedAmount)) {
                resolvedAmounts = data.discount.maximumDiscountAllowedAmount
                Integer resolvedQuantitys = discountDataList.sum{it.resolvedQuantity}
                //Integer resolvedQuantitys = discountDataList.size()
                Double unitDiscount = resolvedAmounts / resolvedQuantitys
                discountDataList.each {
                    it.resolvedAmount = unitDiscount * it.resolvedQuantity
                }
            }

            if(resolvedAmounts > amount) {
                amount = resolvedAmounts
                resolveDataList = discountDataList
                resolveData = data
            }
        }

        if (!resolveData && dataList) {
            resolveData = dataList.get(0)
        }

        response.resolveDataList = resolveDataList
        response.resolveData = resolveData

        return response
    }

    static DiscountData getMaxProductFreeData(List<DiscountData> dataList) {
        Double amount = 0
        DiscountData resolveData
        for(DiscountData data : dataList) {

            Double resolvedAmounts = data.childDatas.sum{it.resolvedAmount}

            if (data.childDatas && (data.discount.isMaximumDiscountAllowed && (resolvedAmounts > data.discount.maximumDiscountAllowedAmount))) {
                resolvedAmounts = data.discount.maximumDiscountAllowedAmount
                Integer resolvedQuantitys = data.childDatas.size()
                Double unitDiscount = resolvedAmounts / resolvedQuantitys
                data.childDatas.each {
                    it.resolvedAmount = unitDiscount
                }
            }

            if(resolvedAmounts > amount) {
                amount = resolvedAmounts
                resolveData = data
            }

        }

        if (!resolveData && dataList) {
            resolveData = dataList.get(0)
        }

        return resolveData
    }

    static DiscountData getMaxQuantityData(List<DiscountData> dataList) {
        Integer quantity = 0
        DiscountData filteredDiscountData
        for(DiscountData data : dataList) {
            if(data.selectedProducts.size() > quantity) {
                quantity = data.selectedProducts.size()
                filteredDiscountData = data
            }
        }
        return filteredDiscountData
    }

    static List<DiscountData> getConfigFilteredDatas(List<DiscountData> dataList) {

        List<DiscountData> canBeUseWithOther = []
        List<DiscountData> canNotBeUseWithOther = []

        if (dataList) {
            for (DiscountData data : dataList) {
                if (data.discount.isDiscountUsedWithOtherDiscount) {
                    canBeUseWithOther.add(data)
                } else {
                    canNotBeUseWithOther.add(data)
                }
            }
        }

        return canBeUseWithOther ?: canNotBeUseWithOther
    }

    static void restoreCartItem (Cart cart) {
        // restore cart item price
        DiscountData selectedDiscountData = cart.selectedDiscountData
        if (selectedDiscountData && selectedDiscountData.isDiscountOnProductAmount) {
            cart.cartItemList.each {item ->
                DiscountData discountData = item.discountData
                if (discountData && discountData.isDiscountOnProductAmount) {
                    //ProductDiscountDetails details = discountData.discount.discountDetails
                    //if (details && details.amountType.equals(Constants.AMOUNT_DETAILS_TYPE.SINGLE)) {
                        ProductData productData = item.object.product

                        productData.isStoreActualInfo = false
                        productData.basePrice = productData.actualBasePrice
                        productData.updatePrice()

                        item.discount = 0
                        item.unitPrice = productData.basePrice
                    //}
                }
            }
        }
    }

}

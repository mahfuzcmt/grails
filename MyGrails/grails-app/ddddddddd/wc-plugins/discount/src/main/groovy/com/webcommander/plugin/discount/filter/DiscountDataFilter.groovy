package com.webcommander.plugin.discount.filter

import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.util.DiscountDataUtil

/**
 * Created by sharif ul islam on 28/03/2018.
 */
class DiscountDataFilter extends Filter {

    @Override
    Object doFilter(Map context) {

        Map response = [:]

        List<DiscountData> dataList = context.dataList

        List<Map> resolvedDataList = []

        if (dataList) {
            dataList = DiscountDataUtil.getConfigFilteredDatas(dataList)
            Constants.DISCOUNT_PRIORITY_MATRIX.each {
                List<DiscountData> foundDatas = getDiscountDatas(it.value, dataList)
                if (foundDatas) {
                    Map foundData = [:]
                    foundData.put(it.value, foundDatas)
                    resolvedDataList.add(foundData)
                }
            }

            if (resolvedDataList) {
                Map resolvedData = resolvedDataList.get(0)
                DiscountData filteredDiscountData
                List<DiscountData> discountDataList

                resolvedData.each {String priority, List<DiscountData> foundDatas ->

                    response.priority = priority

                    if (priority.equals(Constants.DISCOUNT_PRIORITY_MATRIX.PRODUCT_CAPPED_PRICE)) {
                        Map amountResponse = DiscountDataUtil.getMaxProductPriceCapData(foundDatas, dataList)
                        filteredDiscountData = amountResponse.resolveData
                        discountDataList = amountResponse.resolveDataList
                        return false
                    } else if (priority.equals(Constants.DISCOUNT_PRIORITY_MATRIX.PRODUCT_DISCOUNT_AMOUNT)) {
                        Map amountResponse = DiscountDataUtil.getMaxProductAmountData(foundDatas, dataList)
                        filteredDiscountData = amountResponse.resolveData
                        discountDataList = amountResponse.resolveDataList
                        return false
                    } else if (priority.equals(Constants.DISCOUNT_PRIORITY_MATRIX.FREE_PRODUCT)) {
                        filteredDiscountData = DiscountDataUtil.getMaxProductFreeData(foundDatas)
                    } else if (Constants.DISCOUNT_AMOUNT_PRIORITY_MATRIX.any{it -> it == priority}) {
                        filteredDiscountData = DiscountDataUtil.getMaxAmountData(foundDatas)
                    } else {
                        filteredDiscountData = foundDatas.get(0)
                    }

                }

                response.discountDataList = discountDataList
                response.filteredDiscountData = filteredDiscountData
            }

        }

        return response
    }

    List<DiscountData> getDiscountDatas(String priority, List<DiscountData> dataList) {
        List<DiscountData> foundDatas = []
        switch (priority) {
            case Constants.DISCOUNT_PRIORITY_MATRIX.FREE_SHIPPING:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                            && data.discount.discountDetails.type.equals(Constants.SHIPPING_DETAILS_TYPE.FREE_SHIPPING)) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.FREE_PRODUCT:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.PRODUCT)
                            && data.discount.discountDetails.type.equals(Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT)
                            //&& data.isFreeProduct
                        ) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.SHIPPING_CAPPED_PRICE:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                            && data.discount.discountDetails.type.equals(Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP)) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.PRODUCT_CAPPED_PRICE:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.PRODUCT)
                            && data.discount.discountDetails.type.equals(Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP)
                            && data.isPriceCapMainProd
                        ) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.SHIPPING_DISCOUNT_AMOUNT:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.SHIPPING)
                            && data.discount.discountDetails.type.equals(Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT)) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.PRODUCT_DISCOUNT_AMOUNT:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.PRODUCT)
                            && data.discount.discountDetails.type.equals(Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT)
                            && data.isProductAmountMainProd
                        ) {
                        foundDatas.add(data)
                    }
                }
                break
            case Constants.DISCOUNT_PRIORITY_MATRIX.ORDER_DISCOUNT_AMOUNT:
                dataList.each { DiscountData data ->
                    if (data.discount.discountDetailsType.equals(Constants.DETAILS_TYPE.AMOUNT)) {
                        foundDatas.add(data)
                    }
                }
                break
        }

        return foundDatas

    }

}

package com.webcommander.plugin.discount.processor

import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.filter.DiscountFilterFactory
import com.webcommander.plugin.discount.filter.Filter
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.resolver.DiscountResolverFactory
import com.webcommander.plugin.discount.resolver.Resolver
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.plugin.discount.util.DiscountMessageUtil
import com.webcommander.plugin.discount.webcommerce.details.ProductDiscountDetails
import com.webcommander.util.AppUtil
import com.webcommander.util.ResponseUtils
import com.webcommander.webcommerce.TaxCode

/**
 * Created by sharif ul islam on 28/03/2018.
 */
class DiscountProcessor extends Processor {

    @Override
    Object doProcess(Map context) {

        Cart cart = context.cart

        Map response = [:]

        DiscountDataUtil.clearDiscountData(cart)

        Resolver discountResolver = DiscountResolverFactory.discountResolver

        List<DiscountData> dataList = []

        Map resolverResponse = discountResolver.resolve(context)

        if (resolverResponse.discountDataList) {
            for (DiscountData discountData : resolverResponse.discountDataList) {
                if (discountData.resolvedAmount && discountData.discountDetailsType == Constants.DETAILS_TYPE.AMOUNT) {
                    dataList.add(discountData)
                } else if (discountData.resolvedAmount && discountData.discountDetailsType == Constants.DETAILS_TYPE.SHIPPING) {
                    dataList.add(discountData)
                }
            }
        }

        // calculate discount for individual cart item [start]
        AppUtil.session.selectedDiscountData = null
        AppUtil.session.productCapAppliedMap = [:]
        AppUtil.session.prodAmountTiredTotalMap = [:]
        cart.cartItemList.each { CartItem item ->

            ProductData data = item.object.product

            Map resolverContext = [:]
            resolverContext.productId = data.id
            resolverContext.productData = data
            resolverContext.cart = cart
            Map itemResolverResponse = discountResolver.resolve(resolverContext)

            if (itemResolverResponse.discountDataList) {
                for (DiscountData discountData : itemResolverResponse.discountDataList) {
                    if (discountData.isFreeProduct) {
                        dataList.add(discountData)
                    } else if (discountData.isPriceCap) {
                        dataList.add(discountData)
                    } else if (discountData.isDiscountOnProductAmount) {
                        dataList.add(discountData)
                    } else if (discountData.isPriceCapMainProd) {
                        dataList.add(discountData)
                    } else if (discountData.isProductAmountMainProd) {
                        dataList.add(discountData)
                    }
                }
            }

        }

        // calculate discount for individual cart item [end]
        DiscountData discountData
        if (dataList) {

            Filter dataFilter = DiscountFilterFactory.getDiscountDataFilter()

            Map filterContext = [:]
            filterContext.dataList = dataList

            Map filterResponse = dataFilter.filter(filterContext)

            discountData = filterResponse.filteredDiscountData
            List<DiscountData> discountDataList = filterResponse.discountDataList

            DiscountDataUtil.clearDiscountData(cart)

            if (discountData) {

                if (discountData.resolvedAmount && discountData.discountDetailsType == Constants.DETAILS_TYPE.AMOUNT) {

                    cart.discountOnOrder = discountData.resolvedAmount

                    cart.selectedDiscountData = discountData

                } else if (discountData.resolvedAmount && discountData.discountDetailsType == Constants.DETAILS_TYPE.SHIPPING) {

                    cart.discountOnShipping = discountData.resolvedAmount

                    cart.selectedDiscountData = discountData

                } else if (discountData.isFreeProduct) {

                    discountData.childDatas.each { childData ->

                        CartItem cartItem = cart.cartItemList.find() {it.object.product.id == childData.productData.id}
                        if (cartItem) {

                            if (discountData.productData.id == cartItem.object.product.id && cartItem.quantity <= discountData.discount.discountDetails.freeProductMaxQty) {
                                return true
                            }

                            Long taxCodeId = childData.productData.taxCodeId
                            if (taxCodeId) {

                                TaxCode taxCode = TaxCode.get(taxCodeId)

                                Double unitTax = childData.resolvedAmount * taxCode.rate / 100
                                Double unitPrice = childData.resolvedAmount + unitTax
                                unitPrice = new Double ( (unitPrice).toConfigPrice() )

                                cartItem.discount = unitPrice
                                cartItem.actualDiscount = unitPrice

                                cartItem.taxDiscount = unitTax

                            }

                        }
                    }

                    CartItem item = CartManager.getCartItem(discountData.productData, AppUtil.params, cart)

                    discountData.isShowActualPrice = false
                    item.discountData = discountData

                    cart.selectedDiscountData = discountData

                } else if (discountData.isPriceCapMainProd) {

                    discountDataList.each {DiscountData data ->
                        CartItem item = CartManager.getCartItem(data.productData, AppUtil.params, cart)
                        if (item) {
                            //item.discount = data.resolvedAmount
                            item.actualDiscount = data.resolvedAmount

                            data.isShowActualPrice = false
                            item.discountData = data

                            //cart.selectedDiscountData = data
                        }
                    }

                    CartItem item = CartManager.getCartItem(discountData.productData, AppUtil.params, cart)

                    item.discountData = discountData

                    cart.selectedDiscountData = discountData

                } else if (discountData.isProductAmountMainProd) {
                    Double totalDiscount = 0
                    ProductDiscountDetails details = discountData.discount.discountDetails
                    discountDataList.each {DiscountData data ->
                        CartItem item = CartManager.getCartItem(data.productData, AppUtil.params, cart)
                        if (item) {

                            if (details && details.amountType.equals(Constants.AMOUNT_DETAILS_TYPE.SINGLE)) {
                                ProductData productData = item.object.product

                                productData.isStoreActualInfo = false
                                productData.basePrice = productData.actualBasePrice - data.resolvedAmount
                                productData.updatePrice()

                                item.discount = data.resolvedAmount
                                item.unitPrice = productData.basePrice

                                item.discountData = data
                                totalDiscount += data.resolvedAmount
                            } else if (details && details.amountType.equals(Constants.AMOUNT_DETAILS_TYPE.TIERED)) {
                                ProductData productData = item.object.product
                                if(details.minimumQtyOn == Constants.MINIMUM_AMOUNT_ON.EACH_ITEM) {
                                    Long taxCodeId = productData.taxCodeId
                                    if (taxCodeId) {
                                        TaxCode taxCode = TaxCode.get(taxCodeId)

                                        Double baseTotal = (item.unitPrice * item.quantity) - data.resolvedAmount

                                        Double totalTax = baseTotal * taxCode.rate / 100
                                        baseTotal += totalTax
                                        //item.taxDiscount = item.tax - totalTax

                                        item.discount = data.resolvedAmount
                                        item.actualDiscount = item.actualUnitPrice * item.quantity - baseTotal
                                        item.taxDiscount = (item.unitTax * item.quantity) - totalTax

                                        data.isShowActualPrice = false

                                        item.discountData = data
                                        totalDiscount += item.actualDiscount
                                    }
                                } else if(details.minimumQtyOn == Constants.MINIMUM_AMOUNT_ON.TOTAL) {
                                    cart.discountOnOrder = data.resolvedAmount
                                    return false
                                }
                            }
                        }
                    }

                    discountData.resolvedAmount = totalDiscount

                    CartItem item = CartManager.getCartItem(discountData.productData, AppUtil.params, cart)

                    item.discountData = discountData

                    cart.selectedDiscountData = discountData

                }

                if (discountData.discount.isApplyCouponCode) {
                    AppUtil.request.setAttribute("discountCouponStatusMsg", DiscountMessageUtil.getCouponStatusMessage(discountData, ResponseUtils.STATUS_TYPE_SUCCESS))
                    AppUtil.request.setAttribute("discountCouponStatusType", ResponseUtils.STATUS_TYPE_SUCCESS)
                }

            }

        }

        if (AppUtil.params.couponCode && (!discountData || !discountData.discount.isApplyCouponCode)) {
            AppUtil.request.setAttribute("discountCouponStatusMsg", DiscountMessageUtil.getCouponStatusMessage(discountData, ResponseUtils.STATUS_TYPE_ERROR))
            AppUtil.request.setAttribute("discountCouponStatusType", ResponseUtils.STATUS_TYPE_ERROR)
        }

        Long taxCodeId = cart.getUniqueTaxCodeId()
        if (cart.discountOnOrder) {
            if (taxCodeId) {
                TaxCode taxCode = TaxCode.get(taxCodeId)
                Double actualTax = (cart.baseTotal - cart.discountOnOrder) * taxCode.rate / 100
                cart.actualTax = actualTax
            } else {
                cart.discountOnOrder = 0
            }
        }

        CartManager.calcuateCartTotal(cart)
        AppUtil.session.selectedDiscountData = cart.selectedDiscountData

        return response

    }

}

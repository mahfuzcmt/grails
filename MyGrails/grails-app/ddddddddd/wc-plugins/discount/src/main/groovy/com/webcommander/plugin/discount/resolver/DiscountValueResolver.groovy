package com.webcommander.plugin.discount.resolver

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CartManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.models.DiscountData
import com.webcommander.plugin.discount.util.DiscountDataUtil
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.plugin.discount.webcommerce.details.*
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.TaxCode

/**
 * Created by sharif ul islam on 12/03/2018.
 */
class DiscountValueResolver extends Resolver {

    @Override
    Object doResolve(Map context) {

        Map response = [:]

        Cart cart = context.cart
        CustomDiscount discount = context.discount
        Long productId = context.productId
        ProductData productData = context.productData
        DiscountData discountData = context.discountData

        discountData.discountDetailsType = discount.discountDetailsType

        if(discount.discountDetailsType == Constants.DETAILS_TYPE.AMOUNT) {
            this.calculateAmountDiscount(discount.discountDetails, discountData, cart)
        } else if(discount.discountDetailsType == Constants.DETAILS_TYPE.SHIPPING) {
            this.calculateShippingDiscount(discount.discountDetails, discountData, cart)
        } else if(discount.discountDetailsType == Constants.DETAILS_TYPE.PRODUCT) {
            this.calculateProductDiscount(productData, discount.discountDetails, discountData, cart)
        }

        if (discount.isMaximumDiscountAllowed && (discountData.resolvedAmount && discountData.resolvedAmount > discount.maximumDiscountAllowedAmount)) {
            discountData.resolvedAmount = discount.maximumDiscountAllowedAmount
        }

        response.discountData = discountData

        return response
    }

    void calculateProductAmountDiscount(ProductData data, ProductDiscountDetails details, DiscountData discountData, Cart cart) {

        Double unitPrice = data.getCalculatedActualBasePrice()
        Double itemBaseTotal = unitPrice
        Double subTotal = unitPrice

        CartItem item
        if (cart) {
            item = CartManager.getCartItem(data, AppUtil.params, cart)
            if (item) {
                unitPrice = item.unitPrice
                itemBaseTotal = item.unitPrice * item.quantity
                //subTotal = cart.cartItemList.sum { it.baseTotal } ?: 0

                item.discount = 0.0
            }
        }

        Double amount = null
        switch (details.amountType) {
            case Constants.AMOUNT_DETAILS_TYPE.SINGLE:
                amount = this.calculateAmountBasedOnType(details.singleAmount, details.singleAmountType, unitPrice)
                discountData.resolvedQuantity = item.quantity
                break
            case Constants.AMOUNT_DETAILS_TYPE.TIERED:
                if(details.minimumQtyOn == Constants.MINIMUM_AMOUNT_ON.EACH_ITEM) {
                    amount = calculateFromQtyTier(details.tiers, item.quantity, itemBaseTotal)
                    discountData.resolvedQuantity = item.quantity
                } else  {

                    Map prodAmountTiredTotalMap = AppUtil.session.prodAmountTiredTotalMap
                    if (prodAmountTiredTotalMap.get(discountData.discount.id)) {
                        prodAmountTiredTotalMap.put(discountData.discount.id, prodAmountTiredTotalMap.get(discountData.discount.id) + item.quantity)
                    } else {
                        prodAmountTiredTotalMap.put(discountData.discount.id, item.quantity)
                    }

                    Integer tiredQuantity = calculateFromQtyTier(details.tiers, prodAmountTiredTotalMap.get(discountData.discount.id))
                    discountData.resolvedQuantity = tiredQuantity

                    subTotal = 0
                    Integer availableQuantity = tiredQuantity
                    cart.cartItemList.each {cartItem ->
                        if (item.discountData && item.discountData.isDiscountOnProductAmount && availableQuantity) {
                            Integer quantity = cartItem.quantity <= availableQuantity ? cartItem.quantity : availableQuantity
                            availableQuantity = availableQuantity - quantity
                            subTotal += cartItem.unitPrice * quantity
                        }
                    }

                    amount = calculateFromQtyTier(details.tiers, prodAmountTiredTotalMap.get(discountData.discount.id), subTotal)

                }
        }

        discountData.resolvedAmount = amount
    }

    void calculateProductDiscount(ProductData data, ProductDiscountDetails details, DiscountData discountData, Cart cart) {
        Double amount = null

        Double unitPrice = data.getCalculatedActualBasePrice()
        Double itemBaseTotal = unitPrice
        Double subTotal = unitPrice

        CartItem item
        if (cart) {
            item = CartManager.getCartItem(data, AppUtil.params, cart)
            if (item) {
                unitPrice = item.unitPrice
                itemBaseTotal = item.unitPrice * item.quantity
                subTotal = cart.cartItemList.sum { it.baseTotal } ?: 0

                item.discount = 0.0
            }
        }

        switch (details.type) {
            case Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT:
                if (item) {
                    Product prod = DiscountDataUtil.getAllProducts(discountData.discount).find() { it.id == item.object.product.id }
                    if (prod && item.quantity >= details.freeProductMaxQty) {
                        discountData.childDatas.clear()
                        discountData.isFreeProduct = true
                        discountData.selectedProducts = details.getProducts()
                        discountData.selectedProducts.each { freeProduct ->
                            CartItem cartItem = cart.cartItemList.find() {it.object.product.id == freeProduct.id}
                            if (cartItem) {
                                DiscountData childData = new DiscountData()
                                childData.productId = freeProduct.id
                                childData.discountId = discountData.discount.id
                                childData.productData = cartItem.object.product
                                childData.discount = discountData.discount

                                childData.resolvedQuantity = cartItem.quantity
                                childData.resolvedAmount = cartItem.unitPrice

                                discountData.childDatas.add(childData)
                            }
                        }

                    }
                }
                break
            case Constants.PRODUCT_DETAILS_TYPE.DISCOUNT_AMOUNT:
                if (item) {
                    Product prod = details.getProducts().find() { it.id == item.object.product.id }
                    if (prod) {
                        discountData.isDiscountOnProductAmount = true
                        item.discountData = discountData
                        calculateProductAmountDiscount(data, details, discountData, cart)
                        if (!discountData.resolvedAmount) {
                            discountData.isDiscountOnProductAmount = false
                        }
                    } else {

                        discountData.isProductAmountMainProd = true
                        discountData.selectedProducts = details.products

                        item.discountData = discountData
                    }
                }
                break
            case Constants.PRODUCT_DETAILS_TYPE.PRICE_CAP:
                if (item) {
                    Product prod = details.getProducts().find() { it.id == item.object.product.id }
                    if (prod) {
                        calculateProductPriceCap(item, data, details, discountData)
                    } else {

                        discountData.isPriceCapMainProd = true
                        discountData.selectedProducts = details.products

                        item.discountData = discountData
                    }
                }
                break
        }

    }

    void calculateProductPriceCap (CartItem item, ProductData data, ProductDiscountDetails details, DiscountData discountData) {

        Map productCapAppliedMap = AppUtil.session.productCapAppliedMap
        if (!productCapAppliedMap.get(discountData.discount.id)) {
            productCapAppliedMap.put(discountData.discount.id, 0)
        }

        Integer availableQuantity = details.capPriceMaxQty - productCapAppliedMap.get(discountData.discount.id)

        Integer quantity = item.quantity <= availableQuantity ? item.quantity : availableQuantity
        if (quantity >= 0) {
            discountData.isPriceCap = true
            discountData.resolvedQuantity = quantity
            productCapAppliedMap.put(discountData.discount.id, productCapAppliedMap.get(discountData.discount.id) + discountData.resolvedQuantity)

            Long taxCodeId = data.taxCodeId
            if (taxCodeId) {
                TaxCode taxCode = TaxCode.get(taxCodeId)

                Double unitTax = details.capPrice * taxCode.rate / 100
                Double capUnitPrice = new Double( (details.capPrice + unitTax).toConfigPrice() )

                Double amount = (item.actualUnitPrice - capUnitPrice) * discountData.resolvedQuantity

                discountData.resolvedAmount = amount
            }
        }

    }

    void calculateShippingDiscount(ShippingDiscountDetails details, DiscountData discountData, Cart cart) {
        Double amount = null

        Long selectedShippingMethod
        if (cart.cartItemList) {
            selectedShippingMethod = cart.cartItemList.get(0).selectedShippingMethod
        }

        AddressData shippingAddress = AppUtil.effectiveShippingAddress

        Double shippingCost = cart.getShippingCost()?.shipping
        if (!cart.getShippingCost() && shippingAddress) {
            CartManager.updateShippingTotal(cart, shippingAddress, false)
            shippingCost = cart.getShippingCost()?.shipping
        }

        Boolean isClassEnabled = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "enable_shipping_class") == "true"
        if(shippingCost == null || shippingAddress == null || (details.zone && !AppUtil.matchAddressWithZone(shippingAddress, details.zone)) || (isClassEnabled && details.shippingClass && selectedShippingMethod && details.shippingClass.id != selectedShippingMethod)) {
            return
        }

        switch (details.type) {
            case Constants.SHIPPING_DETAILS_TYPE.FREE_SHIPPING:
                amount = shippingCost
                discountData.isFreeShipping = true
                break
            case Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP:
                if(details.capAmount < shippingCost && (shippingCost - details.capAmount) > cart.discountOnShipping) {
                    amount = shippingCost - details.capAmount
                }
                break
            case Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT:
                if(details.amountType == Constants.SHIPPING_DETAILS_AMOUNT_TYPE.SINGLE) {
                    amount = calculateAmountBasedOnType(details.singleAmount, details.singleAmountType, shippingCost)
                } else {
                    amount = calculateFromAmountTier(details.tiers, shippingCost)
                }
                amount = amount > shippingCost ? shippingCost : amount
                break
        }

        discountData.resolvedAmount = amount
    }

    void calculateAmountDiscount(AmountDiscountDetails details, DiscountData discountData, Cart cart) {
        Double amount = null

        switch (details.type) {
            case Constants.AMOUNT_DETAILS_TYPE.SINGLE:
                amount = calculateAmountBasedOnType(details.singleAmount, details.singleAmountType, cart.baseTotal)
                break
            case Constants.AMOUNT_DETAILS_TYPE.TIERED:
                amount = calculateFromAmountTier(details.tiers, cart.baseTotal)
                break
        }
        amount = amount > cart.baseTotal ? cart.baseTotal : amount
        discountData.resolvedAmount = amount
    }

    Double calculateAmountBasedOnType(Double amount, String type, Double relativeAmount) {
        if(type == DomainConstants.SHIPPING_AMOUNT_TYPE.PERCENT) {
            amount = relativeAmount * amount / 100
        }
        return amount
    }

    Double calculateFromAmountTier(List<DiscountAmountTier> tiers, Double checkAmount) {
        Double amount = null
        Double lastMinimumAmount = null
        for(DiscountAmountTier tier : tiers) {
            if(checkAmount >= tier.minimumAmount && tier.minimumAmount > lastMinimumAmount) {
                lastMinimumAmount = tier.minimumAmount
                //amount = tier.amount
                amount = calculateAmountBasedOnType(tier.amount, tier.amountType, checkAmount)

            }
        }
        return amount
    }

    Double calculateFromQtyTier(List<DiscountQtyTier> tiers, Integer checkQty, Double relativeAmount) {
        Double amount = null
        Integer lastMinimumQty = null
        for(DiscountQtyTier tier : tiers) {
            if(checkQty >= tier.minimumQty && tier.minimumQty > lastMinimumQty) {
                amount = calculateAmountBasedOnType(tier.amount, tier.amountType, relativeAmount)
                lastMinimumQty = tier.minimumQty
            }
        }
        return amount
    }

    Integer calculateFromQtyTier(List<DiscountQtyTier> tiers, Integer checkQty) {
        Integer lastMinimumQty = null
        for(DiscountQtyTier tier : tiers) {
            if(checkQty >= tier.minimumQty && tier.minimumQty > lastMinimumQty) {
                lastMinimumQty = tier.minimumQty
            }
        }
        return lastMinimumQty
    }

}

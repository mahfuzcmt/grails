package com.webcommander.plugin.discount.models

import com.webcommander.ApplicationTagLib
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.ProductData
import com.webcommander.plugin.discount.Constants
import com.webcommander.plugin.discount.NameConstants
import com.webcommander.plugin.discount.webcommerce.CustomDiscount
import com.webcommander.util.AppUtil
import com.webcommander.util.TemplateMatcher
import com.webcommander.webcommerce.Product

/**
 * Created by sharif ul islam on 18/03/2018.
 */
class DiscountData {

    Long discountId
    Long productId

    Double resolvedAmount
    Integer resolvedQuantity

    Boolean isFreeShipping = false
    Boolean isFreeProduct = false
    Boolean isPriceCap = false
    Boolean isDiscountOnProductAmount = false
    Boolean isShowActualPrice = true

    Boolean isPriceCapMainProd = false
    Boolean isProductAmountMainProd = false

    String discountDetailsType

    CustomDiscount discount
    ProductData productData

    Collection<Product> selectedProducts = []

    Collection<DiscountData> childDatas = []

    String getDiscountedMessage () {
        String message
        if ((isFreeProduct || isPriceCapMainProd || isProductAmountMainProd) && selectedProducts) {

            // currenty only support for product single amount discount but not tired, will work later
            if (isProductAmountMainProd && !discount.discountDetails.amountType.equals(Constants.AMOUNT_DETAILS_TYPE.SINGLE)) {
                return null
            }

            Cart cart  = CartManager.getCart(AppUtil.session.id)
            List<Product> productToAdd = []
            def g = AppUtil.getBean(ApplicationTagLib)

            selectedProducts.each { freeProduct ->
                CartItem cartItem = cart.cartItemList.find() { it.object.product.id == freeProduct.id }
                if (!cartItem) {
                    productToAdd.add(freeProduct)
                }

                if (cartItem && cartItem.object.product.id == productId
                        && (discount.discountDetails.type == Constants.PRODUCT_DETAILS_TYPE.FREE_PRODUCT && cartItem.quantity <= discount.discountDetails.freeProductMaxQty)) {
                    productToAdd.add(freeProduct)
                }
            }

            if (productToAdd) {

                TemplateMatcher engine = new TemplateMatcher("%", "%")

                String products = ""

                for (Product prod : productToAdd) {
                    products += "<span class='add-to-cart-button button' product-id='"+prod.id+"' cart-min-quantity='1'>"+prod.name.encodeAsBMHTML()+"</span>"
                }

                if (isFreeProduct) {

                    message = discount.isDisplayTextPartialDiscountCondition ? discount.displayTextPartialDiscountCondition : g.message(code: 'discount.free.product.default.message')
                    message = engine.replace(message, [free_product: products])

                } else if (isPriceCapMainProd) {

                    message = discount.isDisplayTextPartialDiscountCondition ? discount.displayTextPartialDiscountCondition : g.message(code: 'discount.price.caped.default.message')
                    message = engine.replace(message, [capped_qty: discount.discountDetails.capPriceMaxQty, capped_product: products, capped_price: AppUtil.currencySymbol+discount.discountDetails.capPrice.toConfigPrice()])

                } else if (isProductAmountMainProd) {
                    String symbol = NameConstants.DISCOUNT_AMOUNT_TYPE[discount.discountDetails.singleAmountType]

                    message = discount.isDisplayTextPartialDiscountCondition ? discount.displayTextPartialDiscountCondition : g.message(code: 'discount.product.amount.default.message')
                    message = engine.replace(message, [discounted_product: products, amt: symbol+discount.discountDetails.singleAmount])

                }

            }
        }

        return message
    }
}

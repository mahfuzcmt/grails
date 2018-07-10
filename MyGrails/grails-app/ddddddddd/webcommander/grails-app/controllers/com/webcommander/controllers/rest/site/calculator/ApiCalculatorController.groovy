package com.webcommander.controllers.rest.site.calculator

import com.webcommander.admin.Customer
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartObject
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.CartManagerExceptionWrap
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.util.TemplateMatcher
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.OrderService
import com.webcommander.webcommerce.PaymentGatewayService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class ApiCalculatorController extends RestProcessor{
    ProductService productService
    OrderService orderService
    PaymentGatewayService paymentGatewayService
    DefaultPaymentService defaultPaymentService

    def orderCostModel() {
        List<Map> items = [];
        Customer customer = AppUtil.loggedCustomer ? Customer.get(AppUtil.loggedCustomer) : null
        Address shippingAddress = params.shippingAddress ? orderService.getAddressFromMap(params.shippingAddress) : customer?.activeShippingAddress
        if(!params.items instanceof List) {
            throw new ApiException("invalid.arguments")
        }
        params.items.each {
            Map item = new HashMap(it)
            Product product = Product.get(it.id)
            item = HookManager.hook("modify-cart-item-data-for-api", item, product)
            item.price = productService.getProductData(product, item.config ?: null).effectivePrice
            items.add(item)
        }
        Cart cart = null
        Map model = [:]
        try {
            cart = CartManager.createCartForAdminOrder(items, [shippingAddress: shippingAddress])
        } catch (CartManagerExceptionWrap ex) {
            response.setStatus(400)
            model.status = "error"
            model.errorItems = [:]
            TemplateMatcher engine = new TemplateMatcher("%", "%")
            ex.exceptions.each {
                CartObject object = it.product
                Map replacerMap = [
                    requested_quantity: it.messageArgs[0],
                    multiple_of_quantity: it.messageArgs[0],
                    maximum_quantity: it.messageArgs[0],
                    minimum_quantity: it.messageArgs[0]
                ]
                String message = it.message
                if(message == "ADD_AVAILABLE") {
                    message = "s:you.can.buy.maximum.quantity.for.product"
                }
                message = site.message(code: message)
                message = engine.replace(message, replacerMap)
                Map item = [
                    id: object.id,
                    name: object.name,
                    errorMessage: message
                ]
                object.modifyApiResponse(item)
                model.errorItems[object.id + ""] = item
            }
        }
        if(cart) {
            model.items = [:]
            cart.sessionId = session.id
            Boolean shouldHaveShipping = cart.cartItemList.find { it.isShippable }
            cart.cartItemList.each {
                model.items[it.object.id + ""] = [
                        id: it.object.id,
                        quantity: it.quantity,
                        unitTax: it.unitTax,
                        tax: it.tax,
                        discount: it.discount,
                        baseToral: it.baseTotal,
                        total: it.total

                ]
            }
            Map shippingCostMap = CartManager.resolveShippingMap(cart)
            if(!shouldHaveShipping) {
                shippingCostMap = [handling: 0, shipping: 0, tax: 0]
            }
            if(shippingAddress && shippingCostMap.shipping == null) {
                throw new ApiException("not.support.shipping.choose.address")
            }
            Double payable = cart.total + (shouldHaveShipping ? (shippingCostMap.handling ?: 0) + (shippingCostMap.shipping ?: 0) + (shippingCostMap.tax ?: 0) : 0)
            model.subTotal = cart.checkoutPageDisplaySubTotal
            if(shippingAddress) {
                model.shippingCost = shippingCostMap.shipping
                model.handlingCost = shippingCostMap.handling
                model.shippingTax = shippingCostMap.tax
            }
            model.totalTax = cart.tax
            model.totalDiscount = cart.discount
            model.grandTotal = payable
            model.payable = payable
            model.cart = cart
            if(params.gatewayCode) {
                Double surcharge = paymentGatewayService.calculateSurchargeAmount(params.gatewayCode, model.payable)
                model.surcharge = surcharge
                model.grandTotal += surcharge
            }
            model = (Map) HookManager.hook("api-order-cost-model", model)
            defaultPaymentService.processDefaultPaymentsForCalculationAPI(model)
            model.remove("cart")
        }
        rest(model)
    }

    /**
     * Deprecated : Would be replaced by orderCostModel
     **/
    def calculateTaxDiscountShipping() {
        orderCostModel();
    }

    def calculateSurcharge() {
        Double amount = paymentGatewayService.calculateSurchargeAmount(params.gatewayCode, params.double("amount"))
        rest(amount: amount)
    }
}

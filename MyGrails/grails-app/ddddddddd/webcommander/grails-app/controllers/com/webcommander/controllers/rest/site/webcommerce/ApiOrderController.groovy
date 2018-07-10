package com.webcommander.controllers.rest.site.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.Cart
import com.webcommander.models.CartObject
import com.webcommander.models.PaymentInfo
import com.webcommander.payment.DefaultPaymentService
import com.webcommander.payment.PaymentService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.tenant.Thread
import com.webcommander.throwables.CartManagerException
import com.webcommander.throwables.PaymentGatewayException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.util.TemplateMatcher
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderService
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.PaymentGatewayMeta
import com.webcommander.webcommerce.PaymentGatewayService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService

class ApiOrderController extends RestProcessor{
    ProductService productService
    OrderService orderService
    PaymentService paymentService
    PaymentGatewayService paymentGatewayService
    DefaultPaymentService defaultPaymentService

    def create() {
        List<Map> items = [];
        if(!params.items instanceof List) {
            throw new ApiException("invalid.arguments")
        }
        Customer customer = Customer.get(AppUtil.loggedCustomer)
        Address billingAddress = params.billingAddress instanceof Map ? orderService.getAddressFromMap(params.billingAddress) : customer?.activeBillingAddress
        if(billingAddress == null) {
            throw new ApiException("billing.address.required")
        }
        Address shippingAddress = params.shippingAddress instanceof Map ? orderService.getAddressFromMap(params.shippingAddress) : customer?.activeShippingAddress
        Map addresses = [shippingAddress: shippingAddress, billingAddress: billingAddress]
        params.items.each {
            Map item = new HashMap(it)
            Product product = Product.get(it.id)
            item = HookManager.hook("modify-cart-item-data-for-api", item, product)
            item.price = productService.getProductData(product, item.config ?: null).effectivePrice
            items.add(item)
        }
        Cart cart
        try {
            cart = CartManager.createCartForAdminOrder(items, addresses)
        } catch (CartManagerException ex) {
            String error = g.message(code: ex.message.substring(2))
            def errorArgs = ex.messageArgs
            CartObject product = ex.product
            if(errorArgs) {
                TemplateMatcher engine = new TemplateMatcher("%", "%")
                Map replacerMap = [
                        requested_quantity: errorArgs[0],
                        multiple_of_quantity: errorArgs[0],
                        maximum_quantity  : errorArgs[0],
                        minimum_quantity  : errorArgs[0],
                        product_name: product.name
                ]
                error = engine.replace(error, replacerMap)
            }
            rest([status: "error", message: error]);
            return;
        }
        if (cart.cartItemList.find { it.isShippable } && cart.getShippingCost().shipping == null) {
            rest([status: "error", message: g.message(code: "not.support.shipping.choose.address")])
            return;
        }
        def orderId = orderService.saveOrder(cart, addresses.billingAddress, addresses.shippingAddress, customer);
        cart.orderId = orderId
        Thread.start {
            AppUtil.initialDummyRequest()
            orderService.sendEmailForOrder(orderId, "create-order")
        }
        Map shippingMap = cart.getShippingCost()
        Boolean shouldHaveShipping = cart.cartItemList.find {
            it.isShippable
        }
        Double payable = cart.total + (shouldHaveShipping ? (shippingMap.handling ?: 0) + (shippingMap.shipping ?: 0) + (shippingMap.tax ?: 0) : 0)
        List<PaymentInfo> payments = new ArrayList<PaymentInfo>()
        Map paymentAsset = [params: params, cart: cart, payable: payable, payments: payments]
        paymentAsset = (Map) HookManager.hook("api-payment-asset", paymentAsset)
        defaultPaymentService.processDefaultPaymentsForAPI(paymentAsset)
        Order order = Order.get(orderId)
        rest(order: order)
    }

    def makePayment() {
        String paymentGateWayCode = params.paymentGateWayCode ?: DomainConstants.PAYMENT_GATEWAY_CODE.CREDIT_CARD
        PaymentGateway gateway = PaymentGateway.findByCode(paymentGateWayCode)
        if(!paymentService.respondsTo("process${paymentGateWayCode}APIPayment") && !gateway.isEnabled) {
            throw new ApiException("payment.gate.way.not.available")
        }
        Long orderId = params.long("orderId")
        Order order = Order.get(orderId)
        Double surcharge = paymentGatewayService.calculateSurchargeAmount(paymentGateWayCode, order.due)
        Payment payment =   orderService.savePayment([
            orderId: orderId,
            paymentGateway: paymentGateWayCode,
            amount: order.due + surcharge,
            paymentStatus: NamedConstants.PAYMENT_STATUS.AWAITING,
            surcharge: surcharge
        ]);
        payment.order.refresh()
        try {
            PaymentInfo paymentInfo = paymentService."process${paymentGateWayCode}APIPayment"(payment, params)
            paymentService.processPostPayment(paymentInfo, paymentInfo.success ? DomainConstants.PAYMENT_STATUS.SUCCESS : DomainConstants.PAYMENT_STATUS.FAILED, payment)
            order = Order.get(orderId)
            if(order.paymentStatus == DomainConstants.ORDER_PAYMENT_STATUS.PAID) {
                AppEventManager.fire("paid-for-order", [order])
            }
            rest(paymentStatus: order.paymentStatus, paid: order.paid, due: order.due)
        } catch (PaymentGatewayException ex) {
            paymentService.processPostPayment(ex.paymentInfo, DomainConstants.PAYMENT_STATUS.FAILED, payment)
            throw new ApiException("Payment Gateway Response: " + ex.message)
        }
    }
}

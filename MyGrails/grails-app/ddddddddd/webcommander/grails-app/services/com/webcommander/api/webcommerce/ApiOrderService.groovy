package com.webcommander.api.webcommerce

import com.webcommander.admin.Customer
import com.webcommander.admin.Operator
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import grails.util.TypeConvertingMap

import java.lang.reflect.Array

@Transactional
class ApiOrderService {
    OrderService orderService
    ProductService productService

    def save(Map orderData) {
        Customer customer = Customer.get(orderData.customerId)
        Order order = new Order();
        order.created = new Date().gmt()
        order.updated = order.created
        order.orderStatus = DomainConstants.ORDER_STATUS.PENDING
        order.shippingStatus = DomainConstants.SHIPPING_STATUS.AWAITING
        order.paymentStatus = DomainConstants.ORDER_PAYMENT_STATUS.UNPAID
        order.billing = orderService.getAddressFromMap(orderData.billingAddress);
        order.shipping = orderData.shippingAddress ? orderService.getAddressFromMap(orderData.shippingAddress) : null;
        order.customerId = customer ? customer.id : null
        order.customerName = customer ? (customer.firstName + (customer.lastName ? " " + customer.lastName : "")) : "Guest Customer";
        order.shippingCost = orderData.shippingCost ?: 0;
        order.handlingCost = orderData.handlingCost ?: 0;
        order.shippingTax = orderData.tax ?: 0;
        order.totalSurcharge = orderData.totalSurcharge ?: 0;
        order.ipAddress = orderData.ip
        if(!orderData.cartItemList || orderData.cartItemList.size() ==0) {
            throw new ApiException("cart.item.list.empty")
        }
        orderData.cartItemList.each {
            OrderItem orderDetails = new OrderItem();
            orderDetails.productId = it.productId
            orderDetails.quantity = it.quantity.toInteger();
            orderDetails.productName = it.productName;
            Product product = productService.getProduct(orderDetails.productId)
            if(product == null) {
                throw new ApiException("product.id.not.found", [orderDetails.productId])
            }
            ProductData productData = productService.getProductData(product)
            ProductInCart productInCart = new ProductInCart(productData, new TypeConvertingMap([quantity: it.quantity]))
            try {
                productInCart.validate(orderDetails.quantity)
            } catch (CartManagerException ex) {
                throw new ApiException("product.id.not.available", [productData.name])
            }
            orderDetails.productType = NamedConstants.CART_OBJECT_TYPES.PRODUCT;
            orderDetails.price = it.price.toDouble();
            orderDetails.tax = it.tax.toDouble();
            orderDetails.discount = it.discount.toDouble();
            orderDetails.order = order;
            orderDetails.isShippable = it.isShippable;
            orderDetails.isTaxable = it.isTaxable;
            orderDetails.variations = it.variations ?: [];
            order.items.add(orderDetails);
        }
        order.billing.save()
        if(order.shipping) {
            order.shipping.save()
        }
        order.createdBy = Operator.get(AppUtil.loggedOperator)
        order.save();
        addApiPayment(order, orderData.paymentStatus ?: DomainConstants.PAYMENT_STATUS.AWAITING)
        AppEventManager.fire("order-create", [order.id])
        if(!order.hasErrors()){
            def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock")
            if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_ORDER) {
                String note = "After order #${order.id}";
                order.items.each {
                    productService.updateStock(it.productId, it.quantity, note)
                }
            }
            return order.id;
        }
        return null
    }

    def addApiPayment(Order order, String paymentStatus) {
        Payment apiPayment = order.payments.find { it.gatewayCode == DomainConstants.PAYMENT_GATEWAY_CODE.API }
        if(!apiPayment) {
            apiPayment = new Payment();
            apiPayment.amount = order.grandTotal
            apiPayment.surcharge = order.totalSurcharge
            apiPayment.order = order
            apiPayment.payingDate = order.created
            apiPayment.gatewayCode = DomainConstants.PAYMENT_GATEWAY_CODE.API
        }
        apiPayment.status = paymentStatus
        if(apiPayment.id) {
            apiPayment.merge()
        } else {
            apiPayment.save();
            order.addToPayments(apiPayment)
            order.save()
        }
        if(!apiPayment.hasErrors()) {
            orderService.paymentService.updateOrderPaymentStatus(order)
            return true
        }
        return false
    }

    Shipment addShipment(Map requestParams) {
        if(requestParams.shipmentItems instanceof Array && requestParams.shipmentItems instanceof List) {
            throw new ApiException("shipment.item.not.found");
        }
        Order order = Order.get(requestParams.order.toLong())
        List shipmentInfo = orderService.getShipmentInfoForOrder(order)
        Shipment shipment = new Shipment(
                method: requestParams.method,
                trackingInfo: requestParams.trackingInfo,
                order: order,
                shippingDate: requestParams.shippingDate.toDate()
        )
        shipment.save()
        requestParams.shipmentItems.each { item ->
            if(item.quantity <= 0) {
                throw new ApiException("invalid.quantity");
            }
            OrderItem orderItem = OrderItem.createCriteria().get {
                eq("id", item.orderItemId.toLong())
                eq("order.id", order.id)
            };
            if(!orderItem) {
                throw new ApiException("invalid.order.item");
            }
            Integer deliveredQuantity = shipmentInfo.find{ it.orderItemId == orderItem.id}?.deliveredQuantity ?: 0 ;
            if( (deliveredQuantity + item.quantity) > orderItem.quantity) {
                throw new ApiException("delivered.quantity.should.not.gt.order.quantity", [orderItem.productName]);
            }
            ShipmentItem shipmentItem = new ShipmentItem(quantity: item.quantity, orderItem: orderItem, shipment: shipment)
            shipmentItem.save()
            if (!shipmentItem.hasErrors()) {
                def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock")
                if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_SHIPMENT) {
                    String note = "After shipment for order #${orderItem.order.id}";
                    productService.updateStock(shipmentItem.orderItem.productId, shipmentItem.quantity, note);
                }
            }
        }
        orderService.updateOrderShipmentStatus(shipment.order);
        return shipment
    }
}

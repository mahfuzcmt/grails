package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.admin.RoleService
import com.webcommander.api.webcommerce.ApiOrderService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.constants.DomainConstants
import com.webcommander.models.RestrictionPolicy
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderComment
import com.webcommander.webcommerce.OrderService
import com.webcommander.webcommerce.Payment
import com.webcommander.webcommerce.Shipment
import org.apache.commons.httpclient.HttpStatus

class ApiOrderAdminController extends RestProcessor {
    ApiOrderService apiOrderService
    OrderService orderService
    RoleService roleService

    def count() {
        Integer count = orderService.getOrderCount(params);
        rest count: count
    }

    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        Map config = [
            items: [
               details: true,
               marshallerExclude: ["order", "totalPriceConsideringConfiguration",]
            ]
        ]
        if(!session.super_vendor && !roleService.isPermitted(AppUtil.loggedOperator, new RestrictionPolicy(type: "order", permission: "view.list"), [:])) {
            params.operatorId = AppUtil.loggedOperator
        }
        List<Order> orders = orderService.getOrders(params);
        rest([orders: orders], config)
    }

    @Restriction(permission = "order.view")
    def info() {
        Order order = Order.get(params.id);
        if(!order) {
            throw new ApiException("order.not.found", HttpStatus.SC_NOT_FOUND)
        }
        Map config = [
            items: [
                details: true,
                marshallerExclude: ["order", "totalPriceConsideringConfiguration"]
            ],
            billing: [details: true],
            shipping: [details: true]
        ]
        rest order: order, config
    }

    @Restriction(permission =  "order.create")
    def create() {
        Map orderData =  request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        Long orderId = apiOrderService.save(orderData);
        rest orderId: orderId
    }

    @Restriction(permission = "order.manage.order", entity_param = "id", domain = Order, owner_field = "createdBy")
    def changeOrderStatus() {
        if(orderService.changeOrderStatus(params)) {
            rest status: "success"
        } else {
            throw new ApiException("change.status.error")
        }
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def shipmentList() {
        Order order = Order.get(params.orderId);
        List<Shipment> shipments = order.shipments
        Map config = [
           shipmentItem:[details: true]
        ]
        rest([shipments: shipments], config)
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def shipmentTrackingInfo() {
        Order order = Order.get(params.orderId);
        List infos = order.shipments.collect { [method: it.method, tracking_no: it.trackingInfo] }
        rest([trackingInfo: infos])
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def shipmentAdd() {
        Map requestData =  request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY);
        Shipment shipment = apiOrderService.addShipment(requestData)
        rest status: "success", id: shipment.id
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def paymentList() {
        Order order = Order.get(params.orderId);
        List<Payment> payments = order.payments
        rest([payments: payments])
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def paymentMake() {
        Payment payment = orderService.savePayment(params)
        if(payment) {
            rest([status: "success", id: payment.id])
        } else {
            throw new ApiException("make.payment.error")
        }
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def paymentRefund() {
        Payment payment = Payment.get(params.long("id"))
        if(orderService.refundPayment(payment)) {
            Boolean reAdjustInventory = false;
            def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock");
            if (payment.order.paid == 0.0 && config == DomainConstants.UPDATE_STOCK.AFTER_PAYMENT) {
                reAdjustInventory = true;
            }
            rest status: "success", haveToReAdjustInvetory: reAdjustInventory, id: payment.id
        } else {
            throw new ApiException("payment.refund.error")
        }
    }

    def inventoryReAdjust() {
        Long orderId = params.long("orderId");
        Long paymentId = params.long("paymentId");
        if (orderService.reAdjustInventory(orderId, paymentId)) {
            rest status: "success"
        }else {
            throw new ApiException("inventory.adjust.failure")
        }
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def changePaymentStatus() {
        String status = params.status;
        if(!DomainConstants.ORDER_STATUS[status]) {
            throw new ApiException("invalid.parameters")
        }
        Order order = Order.get(params.orderId);
        Boolean result = apiOrderService.addApiPayment(order, status)
        rest status: result ? "success" : "failed"
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def sendInvoice() {
        try {
            orderService.sendEmailForOrder(params.orderId.toLong(), "send-invoice")
        } catch (Exception ex) {
            throw new ApiException("email.could.not.be.sent")
        }
        rest status: "success"
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def commentHistory() {
        Order order = Order.get(params.orderId)
        List<OrderComment> comments = OrderComment.findAllByOrder(order)
        rest comments: comments
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def commentAdd() {
        Order order = Order.get(params.orderId)
        params.email = order.billing.email;
        OrderComment comment = orderService.sendOderComment(order, params)
        if(comment && !comment.hasErrors()) {
            rest status: "success", id: comment.id
        } else {
            throw new ApiException("send.comment.fail")
        }
    }

}

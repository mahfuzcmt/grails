package com.webcommander.webcommerce

import com.webcommander.admin.Country
import com.webcommander.admin.Customer
import com.webcommander.admin.RoleService
import com.webcommander.admin.State
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.config.StoreDetail
import com.webcommander.config.StoreProductAssoc
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.DocumentService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.payment.PaymentService
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.Base64Coder
import com.webcommander.util.StringUtil
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.SessionFactory
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Order as CriterionOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.interceptor.TransactionAspectSupport

@Initializable
@Transactional
class OrderService {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    CommonService commonService
    CommanderMailService commanderMailService
    PaymentService paymentService
    ProductService productService
    RoleService roleService
    DocumentService documentService
    SessionFactory sessionFactory

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    static void initialize() {
        AppEventManager.on("before-operator-delete", { id ->
            Order.createCriteria().list {
                eq("createdBy.id", id)
            }.each {
                it.createdBy = null
                it.merge()
            }
        })
    }

    Address copyAddress(AddressData addressData) {
        Address address = new Address(addressData.properties);
        address.country = Country.proxy(addressData.countryId)
        if(addressData.stateId) {
            address.state = State.proxy(addressData.stateId)
        }
        return address
    }

    def saveOrder(Cart cart, Address billingAddress, Address shippingAddress, Customer customer) {
        return saveOrder(cart, billingAddress ? new AddressData(billingAddress) : null, shippingAddress ? new AddressData(shippingAddress) : null, customer)
    }

    def saveOrder(Cart cart, AddressData billingAddress, AddressData shippingAddress, Customer customer) {
        Order order
        if(cart.orderId) {
            order = Order.get(cart.orderId)
            if(order.shipments) {
                throw new ApplicationRuntimeException("order.not.modified.shipment.exist")
            }
            AppEventManager.fire("before-order-update", [order.id])
            order.billing.delete()
            order.shipping?.delete()
            order.items*.delete()
            order.items.clear()
        } else {
            order = new Order();
            order.created = new Date().gmt()
            order.updated = order.created
            order.orderStatus = DomainConstants.ORDER_STATUS.PENDING
            order.shippingStatus = DomainConstants.SHIPPING_STATUS.AWAITING
            order.paymentStatus = DomainConstants.ORDER_PAYMENT_STATUS.UNPAID
        }
        order.billing = copyAddress(billingAddress);
        order.deliveryType = cart.deliveryType
        Boolean shouldHaveShipping = cart.cartItemList.find {
            it.isShippable
        }
        order.shipping = shouldHaveShipping && cart.deliveryType != DomainConstants.ORDER_DELIVERY_TYPE.STORE_PICKUP ? copyAddress(shippingAddress) : null;
        order.customerId = customer ? customer.id : null
        order.customerName = customer ? (customer.firstName + (customer.lastName ? " " + customer.lastName : "")) : "Guest Customer";
        def applicableShippingMap = shouldHaveShipping ? cart.shippingCost : [:]
        if(shouldHaveShipping && !applicableShippingMap && cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING) {
            throw new ApplicationRuntimeException("ship.location.not.supported")
        }
        if(cart.deliveryType == DomainConstants.ORDER_DELIVERY_TYPE.SHIPPING) {
            order.shippingCost = applicableShippingMap.shipping ?: 0;
            order.handlingCost = applicableShippingMap.handling ?: 0;
            order.shippingTax = applicableShippingMap.tax ?: 0;
        }
        order.discountOnShipping = cart.discountOnShipping
        order.discountOnOrder = cart.discountOnOrder
        order.actualTax = cart.actualTax
        def request = AppUtil.request
        order.ipAddress = request.ip
        order.billing.save()
        if(order.shipping) {
            order.shipping.save()
        }
        order.save()
        cart.cartItemList.each {
            AppEventManager.fire("before-order-item-create", [order, it])
            OrderItem orderDetails = new OrderItem();
            orderDetails.productId = it.object.id;
            orderDetails.shippingClassId = it.selectedShippingMethod
            orderDetails.productName = it.object.name;
            orderDetails.productType = it.object.type;
            orderDetails.shippingClassName = orderDetails.shippingMethodName
            orderDetails.quantity = it.quantity;
            orderDetails.price = it.unitPrice;
            orderDetails.tax = it.unitTax * it.quantity;
            orderDetails.discount = it.discount;
            orderDetails.actualDiscount = it.actualDiscount
            orderDetails.actualPrice = it.displayUnitPrice
            orderDetails.taxDiscount = it.taxDiscount
            orderDetails.isShippable = it.isShippable;
            orderDetails.isTaxable = it.isTaxable;
            orderDetails.variations = it.variations;
            orderDetails.storeId = it.storeId;
            order.addToItems(orderDetails)
            orderDetails.save()
            AppEventManager.fire("after-order-save", [orderDetails, it, "order"])
            AppEventManager.fire("order-item-create", [orderDetails, it])
        }
        order.save()
        if(order.hasErrors()) {
            TransactionAspectSupport.currentTransactionInfo().transactionStatus.setRollbackOnly()
        }
        if(cart.orderId) {
            AppEventManager.fire("order-update", [order.id])
        } else {
            AppEventManager.fire("order-create", [order.id])
        }
        if(!order.hasErrors()){
            def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock")
            if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_ORDER) {
                String note = "After order #${order.id}";
                order.items.each {
                    if(it.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                        productService.updateStock(it.productId, it.quantity, note, [orderItemId: it.id, variations: it.variations])
                    }
                }
            }
            return order.id;
        }
    }

    def sendEmailForOrder(Long orderId, String identifier, String comment = null) {
        Order.withNewSession {
            Order order = Order.get(orderId)
            Long storeId
            String ccAddress = null
            if ((AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "enable_multi_model") == "true")) {
                def orderStoreMap = order.items.groupBy { _orderItem -> _orderItem.storeId }
                if (orderStoreMap.size() == 1) {
                    storeId = orderStoreMap.values().first().first().storeId
                    ccAddress = storeId ? StoreDetail.findById(storeId).address.email : null
                }
            }
            Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier(identifier, storeId)
            if(!macrosAndTemplate.emailTemplate.active) {
                return;
            }
            Map refinedMacros = macrosAndTemplate.commonMacros
            Map textMacros = [:];
            Map htmlMacros = [:]
            order.attach()
            macrosAndTemplate.macros.each {
                switch (it.key.toString()) {
                    case "order_details":
                        Map orderDetails = [:]
                        orderDetails.items = [];
                        order.items.each {
                            Map item = [:]
                            item.product_name = it.productName.encodeAsBMHTML();
                            String url;
                            String itemNumber = null
                            if(it.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                                Product product = Product.get(it.productId);
                                itemNumber = product?.sku
                                url = product ? app.baseUrl() + "product/" + product.url : "";
                                if(product?.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE && order.paymentStatus == DomainConstants.ORDER_PAYMENT_STATUS.PAID) {
                                    Long variationId = HookManager.hook("get-order-item-variation-id", it)?:null
                                    item.product_download_url = app.baseUrl() + "page/downloadProduct?token=" + productService.getProductFileDownloadToken(it,variationId)
                                    item.is_downloadable =  true
                                } else {
                                    item.isDownloadable =  false
                                }
                            } else {
                                url = HookManager.hook("orderEmailItemUrl", url)
                            }
                            item.item_number = itemNumber
                            item.url = url;
                            item.variations = it.variations.join(", ").encodeAsBMHTML();
                            item.price = it.displayPrice.toPrice();
                            item.quantity = it.quantity;
                            item.discount = it.discount.toPrice();
                            item.tax = it.tax.toPrice();
                            item.total_with_tax_with_discount = it.totalAmount.toPrice();
                            item.total_with_tax_without_discount = (it.totalAmount + it.discount).toPrice();
                            item.total_without_tax_with_discount = (it.totalAmount - it.tax).toPrice();
                            item.total_without_tax_without_discount = it.totalPrice.toPrice();
                            item = HookManager.hook("order-item-email-template-macro", item,"order",it)
                            orderDetails.items.add(item)
                        }
                        orderDetails.sub_total = order.subTotal.toPrice();
                        orderDetails.total_discount = order.totalDiscount.toPrice();
                        orderDetails.total_tax = order.totalTax.toPrice();
                        orderDetails.total_shipping_cost = order.shippingCost.toPrice();
                        orderDetails.shipping_tax = order.shippingTax.toPrice();
                        orderDetails.handling_cost = order.handlingCost.toPrice();
                        orderDetails.payment_surcharge = order.totalSurcharge.toPrice();
                        orderDetails.total = order.grandTotal.toPrice();
                        orderDetails.paid = order.paid.toPrice();
                        orderDetails.due = order.due.toPrice();
                        refinedMacros[it.key] = orderDetails;

                        refinedMacros["sub_total"]=order.subTotal.toPrice();
                        refinedMacros["total_discount"]=order.totalDiscount.toPrice();
                        refinedMacros["total_tax"]=order.totalTax.toPrice();
                        refinedMacros["total_shipping_cost"]=order.shippingCost.toPrice();
                        refinedMacros["shipping_tax"]=order.shippingTax.toPrice();
                        refinedMacros["handling_cost"]=order.handlingCost.toPrice();
                        refinedMacros["payment_surcharge"]=order.totalSurcharge.toPrice();
                        refinedMacros["total"]=order.grandTotal.toPrice();
                        refinedMacros["paid"]=order.paid.toPrice();
                        refinedMacros["due"]=order.due.toPrice();
                        break;
                    case "payment_details":
                        List paymentDetails = [];
                        order.payments.each {
                            Map payment = [:];
                            payment.date= it.payingDate.toEmailFormat();
                            payment.payment_method = g.message(code: PaymentGateway.findByCode(it.gatewayCode).name) ;
                            payment.track_info = it.trackInfo;
                            payment.payer_info = it.payerInfo;
                            payment.status = g.message(code: it.status);
                            payment.amount = it.amount.toPrice();
                            paymentDetails.add(payment);
                        }
                        refinedMacros[it.key] = paymentDetails
                        break;
                    case "billing_address":
                        refinedMacros[it.key] = commonService.addressToMap(order.billing);
                        break;
                    case "shipping_address":
                        refinedMacros[it.key] = order.shipping ? commonService.addressToMap(order.shipping) : null
                        break;
                    case "shipping_details":
                        List shipmentDetails = []
                        order.shipments.each {Shipment shipment ->
                            shipment.shipmentItem.each { ShipmentItem item ->
                                Map entry = [:];
                                entry.product_name = item.orderItem.productName.encodeAsBMHTML();
                                entry.order_quantity = item.orderItem.quantity;
                                entry.shipment_method = g.message( code: shipment.method);
                                entry.shipped_quantity = item.quantity;
                                entry.track_info = item.trackingInfo ?: shipment.trackingInfo;
                                shipmentDetails.add(entry);
                            }
                        }
                        refinedMacros[it.key] = shipmentDetails;
                        break;
                    case "order_id":
                        refinedMacros[it.key] = order.id;
                        break;
                    case "order_date":
                        refinedMacros[it.key] = order.created.toFormattedString("YYYY-MM-dd", false, null, null, null);
                        break;
                    case "customer_name":
                        refinedMacros[it.key] = order.customerName;
                        break;
                    case "customer_login_url":
                        refinedMacros[it.key] = refinedMacros["store_url"] + "customer/login";
                        break;
                    case "payment_info":
                        String paymentInfo = order.payments ? PaymentGateway.findByCode(order.payments.last().gatewayCode).information : "";
                        htmlMacros[it.key] = paymentInfo
                        textMacros[it.key] = paymentInfo ? paymentInfo.textify() : "";
                        break
                    case "currency_symbol":
                        refinedMacros[it.key] = AppUtil.baseCurrency.symbol;
                    case "order_comment":
                        refinedMacros[it.key] = comment ?: "";
                }
            }
            refinedMacros = HookManager.hook("order-mail-macros", refinedMacros, orderId, identifier);
            if(macrosAndTemplate.emailTemplate.active) {
                List attachments = []
                Map macros = new LinkedHashMap(refinedMacros)
                macros << htmlMacros
                if(identifier.equalsIgnoreCase("send-invoice")) {
                    Map attachment = documentService.getPdfAttachment("send-invoice", "Invoice_" + orderId, macros)
                    if(attachment) {
                        attachments.add(attachment)
                    }
                } else if(identifier.equalsIgnoreCase("shipment-complete")) {
                    Map attachment = documentService.getPdfAttachment("shipment-complete", "Shipment_" + orderId, macros)
                    if(attachment) {
                        attachments.add(attachment)
                    }
                } else if(identifier.equalsIgnoreCase("create-order")) {
                    Map attachment = documentService.getPdfAttachment("create-order", "Order_" + orderId, macros)
                    if(attachment) {
                        attachments.add(attachment)
                    }
                }
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, order.billing.email, ccAddress, null, null, attachments, htmlMacros, textMacros, false)
            }
        }
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session
        return {
            if (params.searchText) {
                or {
                    like "customerName", "%" + params.searchText.encodeAsLikeText() + "%"
                    if(params.searchText.matches(/^\d+$/)) {
                        eq "id", params.searchText.toLong()
                    }
                }
            }

            if (params.orderFrom) {
                Date date = params.orderFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.orderTo) {
                Date date = params.orderTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }

            if(params.customerId) {
                eq("customerId", params.customerId.toLong())
            }

            if (params.customerName) {
                like "customerName", "%" + params.customerName.encodeAsLikeText() + "%"
            }
            if (params.orderStatus) {
                eq("orderStatus", params.orderStatus)
            } else {
                ne("orderStatus", "cancelled")
            }
            if (params.shippingStatus) {
                eq("shippingStatus", params.shippingStatus)
            }
            if(params.orderId) {
                eq("id", params.orderId.toLong())
            }
            if (params.paymentStatus){
                eq("paymentStatus", params.paymentStatus)
            }
            if(params.operatorId) {
                eq("createdBy.id", params.operatorId)
            }
            if (params.productName || params.productSku || params.productId) {
                inList "id", Order.where {
                    projections {
                        distinct "id"
                    }
                    items {
                        if(params.productName) {
                            like "productName", "%" + params.productName.encodeAsLikeText() + "%"
                        }
                        if(params.productId) {
                            eq("productId", params.productId.toLong())
                            eq("productType", NamedConstants.CART_OBJECT_TYPES.PRODUCT)
                        }
                        if(params.productSku) {
                            def o = OrderItem
                            exists Product.where {
                                def p = Product
                                eqProperty "o.productId", "p.id"
                                eq "sku", params.productSku
                            }.id()
                        }
                    }
                }
            }
            if (params.total) {
                String operator = params.orderTotalStatus
                if (operator == "greater") {
                    operator = ">"
                } else if (operator == "less") {
                    operator = "<"
                } else {
                    operator = "="
                }
                sqlRestriction("(this_.shipping_cost + this_.shipping_tax + this_.handling_cost + this_.total_surcharge + (select sum(item.quantity * item.price + item.tax - item.discount) from order_item item" + " where item.order_id = this_.id))" + operator + params.total)
            }
        }
    }

    public List<Order> getOrders (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return Order.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            if(params.sort == "total") {
                addOrder(new CriterionOrder("total", params.dir) {
                    @Override
                    String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
                        return "(this_.shipping_cost + this_.shipping_tax + this_.handling_cost + this_.total_surcharge + (select sum(item.quantity * item.price + item.tax - item.discount) from " +
                        "order_item item" + " where item.order_id = this_.id)) " + (params.dir ?: "asc")
                    }
                })
            } else {
                order(params.sort ?: "id", params.dir ?: "desc")
            }
        }
    }

    def saveOrderAddress(Map params) {
        Address address = params.id ? Address.get(params.id) : new Address();
        address.firstName = params.firstName
        address.lastName = params.lastName
        address.addressLine1 = params.addressLine1
        address.addressLine2 = params.addressLine2
        address.postCode = params.postCode
        address.phone = params.phone
        address.mobile = params.mobile
        address.fax = params.fax
        address.email = params.email
        address.country = Country.get(params.country.id)
        address.state = State.get(params.state ? params.state.id : 0)
        address.city = params.city
        address.save();
        return !address.hasErrors();
    }

    public Integer getOrderCount(Map params) {
        Integer count = Order.createCriteria().count {
            and getCriteriaClosure(params)
        }
        return count
    }

    List getOrderItemsList(Map params, Order order) {
        def listMap = [max: params.max, offset: params.offset];
        return OrderItem.createCriteria().list(listMap) {
            eq("order", order)
        }
    }

    List getPaymentList(Map params, Order order) {
        def listMap = [max: params.max, offset: params.offset];
        return Payment.createCriteria().list (listMap){
            eq("order", order)
        }
    }

    Order getOrder(Long id) {
        return Order.get(id)
    }

    List getShipmentInfoForOrder(Order order) {
       return Shipment.createCriteria().list {
           createAlias("shipmentItem", "si")
           createAlias("si.orderItem", "sioi")
           projections {
               groupProperty("sioi.id")
               sum("si.quantity")
               property("method")
           }
           eq("order", order)
       }.collect{
           [orderItemId: it[0], deliveredQuantity: it[1], method: it[2]]
       }
    }

    List getShipmentItems(Shipment shipment) {
        return ShipmentItem.createCriteria().list {
            eq "shipment.id", shipment.id
        }
    }

    boolean saveShipment(Map params) {
        Shipment shipment = Shipment.get(params.shipmentId)
        Order order = Order.get(params.order.toLong())
        List shippedItems = shipment ? getShipmentItems(shipment) : null
        def historyMap = [:]
        boolean error = false
        List orderItem =  params.list("orderItem").collect{it.toLong()}
        Long shipmentQuantity = 0
        orderItem.each {
            shipmentQuantity += it.toLong()
        }
        List quantity =  params.list("quantity").collect{ it == '' ? 0 : it.toLong()}
        def orderTrackingInfos = []
        AddressData shippingAddress = new AddressData(order.shipping)
        orderItem.eachWithIndex{ item, index ->
            def orderTrackingInfo = []
            for(int i = 0; i < quantity[index]; i++){
                Map orderResponse = [:]
                orderResponse = HookManager.hook("${params.method}-save-shipment", orderResponse, shippingAddress, productService.getProduct(OrderItem.get(item).productId), params.pickupDate, params.productDescription, params.receiverInstruction)
                if(orderResponse.size() > 0 && orderResponse.trackingInfo) {
                    orderTrackingInfo.add(orderResponse.trackingInfo)
                }
            }
            if(orderTrackingInfo) {
                orderTrackingInfos.add(orderTrackingInfo.join(", "))
            }
        }
        def session = AppUtil.session
        if(shipment) {
            int totalChanges = ShipmentChangeHistory.createCriteria().list {
                eq "shipment.id", shipment.id
                projections {
                    groupProperty('uuid')
                }
            }.size()
            historyMap.historyLabel = "Changes " + (totalChanges + 1)  + " " + new Date().toAdminFormat(false, false, session.timezone).toString()
            historyMap.uuid = StringUtil.uuid
            historyMap.previousDate = shipment.shippingDate
            historyMap.previousMethod = shipment.method
            historyMap.previousTrack = shipment.trackingInfo
            historyMap.shipment = shipment
            historyMap.changeNote = params.changeNote
        } else {
            shipment = new Shipment()
        }
        shipment.method = params.method
        shipment.trackingInfo = params.trackingInfo
        shipment.shippingDate = params.shippingDate.toDate().gmt(session.timezone)
        order.addToShipments(shipment)
        shipment.save()

        orderItem.eachWithIndex { item, i->
            if(quantity) {
                ShipmentChangeHistory history = null
                OrderItem oItem = OrderItem.get(item)
                ShipmentItem shipmentItem = shippedItems ? shippedItems.find() {it.orderItem.id == oItem.id} : null
                if(shipmentItem) {
                    history = new ShipmentChangeHistory(historyMap)
                    history.changedDate = shipment.shippingDate
                    history.changedMethod = shipment.method
                    history.changedTrack = shipment.trackingInfo
                    history.previousQuantity = shipmentItem.quantity
                    history.orderItem = oItem
                    history.changedQuantity = quantity[i]
                } else {
                    shipmentItem = new ShipmentItem()
                }
                shipmentItem = shipmentItem ?: new ShipmentItem()
                shipmentItem.quantity = quantity[i]
                shipmentItem.orderItem = oItem
                shipmentItem.shipment = shipment
                if(orderTrackingInfos){
                    shipmentItem.trackingInfo = orderTrackingInfos[i]
                }
                shipmentItem.save(flush: true)
                if(shipment.hasErrors()) {
                    error = true
                }
                if (!shipmentItem.hasErrors()) {
                    history?.save(flush: true)
                    def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock")
                    if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_SHIPMENT) {
                        String note = "After shipment for order #${oItem.order.id}";
                        productService.updateStock(shipmentItem.orderItem.productId, shipmentItem.quantity, note, [orderItemId: oItem.id, variations: oItem.variations]);
                    }
                }
            }
        }
        if (!error) {
            updateOrderShipmentStatus(order)
        }
        return !error
    }

    boolean refundPayment(Payment payment) {
        payment.status = NamedConstants.PAYMENT_STATUS.REFUNDED
        Order order = payment.order
        order.totalSurcharge -= payment.surcharge
        payment.merge()
        if(!payment.hasErrors()) {
            paymentService.updateOrderPaymentStatus(payment.order)
            return true;
        }
        return false;
    }

    Payment savePayment(Map params) {
        Long id = 0
        if(params.id) {
            id = params.id.toLong()
        }
        Payment payment
        Order order = Order.get(params.orderId.toLong())
        if (id) {
            payment = Payment.get(id)
        } else {
            payment = new Payment()
            order.addToPayments(payment)
        }
        payment.payingDate = params.date?.toDate()?.gmt(AppUtil.session.timezone) ?: new Date().gmt(AppUtil.session.timezone)
        payment.amount = params.amount.toDouble()
        payment.trackInfo = params.trackInfo
        payment.payerInfo = params.payerInfo
        payment.status = params.paymentStatus ?: NamedConstants.PAYMENT_STATUS.SUCCESS
        payment.gatewayCode = params.paymentGateway
        payment.surcharge = params.surcharge ? params.surcharge.toDouble() : 0
        payment.save()
        if(!payment.hasErrors()) {
            if(payment.status == NamedConstants.PAYMENT_STATUS.SUCCESS) {
                order.totalSurcharge += payment.surcharge
                AppEventManager.fire("after-payment-done", [order])
            }
            paymentService.updateOrderPaymentStatus(order);
            AppEventManager.fire('paid-for-order', [order])
            if(order.paymentStatus == DomainConstants.ORDER_PAYMENT_STATUS.PAID) {
                Thread.start {
                    Thread.sleep(10000) // let the transaction be flushed
                    AppUtil.initialDummyRequest()
                    sendEmailForOrder(order.id, "payment-success")
                }
            }
            return payment;
        }
        return null;
    }

    Boolean changeOrderStatus(Map params) {
        List<Long> ids = params.list("id")*.toLong();
        int count = 0;
        ids.each {
            Order order = Order.get(it)
            order.orderStatus = params.status
            order.save();
            if(!order.hasErrors()) {
                AppEventManager.fire("order-" + params.status, [order.id])
                count++;
            }
        }
        if(count) {
            return true;
        }
        return false;
    }

    public void updateOrderShipmentStatus(Order order) {
        Integer count = 0;
        String identifier;
        order.items.each {
            List<ShipmentItem> items = ShipmentItem.findAllByOrderItem(it)
            Integer shipped = items.sum { it.quantity }
            if (shipped == it.quantity) {
                count ++;
            }
        }
        if(count == order.items.size()) {
            order.shippingStatus = DomainConstants.SHIPPING_STATUS.COMPLETED
            identifier = "shipment-complete";
        } else if (order.shipments.size() > 0) {
            order.shippingStatus = DomainConstants.SHIPPING_STATUS.PARTIAL
            identifier = "partial-shipment"
        } else {
            order.shippingStatus = DomainConstants.SHIPPING_STATUS.AWAITING
        }
        order.save()
        if (identifier) {
            Thread.start {
                Thread.sleep(1000) // let the transaction be flushed
                AppUtil.initialDummyRequest()
                sendEmailForOrder(order.id, identifier)
            }
        }
    }

    public Boolean reAdjustInventory(Long orderId, Long paymentId) {
        Order order = Order.get(orderId);
        Payment payment = order.payments.find { it.id == paymentId }
        if (order.due == 0 && payment.status != DomainConstants.PAYMENT_STATUS.REFUNDED)
            return false;
        String note = "After refund payment #${payment.id} for order #${order.id}";
        order.items.each {
            productService.updateStock(it.productId, it.quantity * -1, note, [orderItemId: it.id, variations: it.variations])
        }
        return true;
    }

    public List<ShipmentItem> getShipmentItems(Map params) {
        return ShipmentItem.createCriteria().list([max: params.max, offset: params.offset]) {
            inList("shipment.id", params.shipmentList)
        }
    }

    public Integer getShipmentItemCount(params) {
        return ShipmentItem.createCriteria().count {
            inList("shipment.id", params.shipmentList)
        }
    }

    Address getAddressFromMap(Map addressMap) {
        Country country = addressMap.countryCode ? Country.findByCode(addressMap.countryCode) : Country.get(addressMap["countryId"])
        State state = addressMap.stateCode ? State.findByCodeAndCountry(addressMap.stateCode, country) : State.get(addressMap["stateId"])
        return new Address(
                firstName: addressMap.firstName,
                lastName: addressMap.lastName,
                addressLine1: addressMap.addressLine1,
                addressLine2: addressMap.addressLine2,
                postCode: addressMap.postCode,
                city: addressMap.city,
                phone: addressMap.phone,
                mobile: addressMap.mobile,
                fax: addressMap.fax,
                email: addressMap.email,
                country: country,
                state: state
        )
    }

    Address getAddressFromJson(String jsonString) {
        JsonSlurper slurper = new JsonSlurper()
        Map addressMap = slurper.parseText(jsonString)
        return  getAddressFromMap(addressMap)
    }

    public OrderComment sendOderComment(Order order, Map params) {
        if(!order) {
            return null
        }
        def storeDetail = StoreDetail.first()
        OrderComment comment = new OrderComment(order: order, adminName: storeDetail.name)
        comment.content = params.message
        if(!params.saveNsend) {
            comment.isVisibleToCustomer = false
        }
        comment.save()
        if(params.saveNsend) {
            sendAdminOrderCommentMail(order, params)
        }
        return comment
    }

    def sendAdminOrderCommentMail(Order order, Map params) {
        if(order.customerId) {
            params.link = app.siteBaseUrl() + "customer/profile";
        } else {
            params.link = app.siteBaseUrl() + "orderComment?token=" + Base64Coder.encode(order.id + "::::" + order.customerName ?: "")
        }
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("admin-order-comment-notification")
        if (!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name" :
                    refinedMacros[it.key] = order.customerName ?: app.message(code: "customer")
                    break;
                case "order_id" :
                    refinedMacros[it.key] = params.orderId
                    break;
                case "comment_link" :
                    refinedMacros[it.key] = params.link
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, params.email)
    }
}

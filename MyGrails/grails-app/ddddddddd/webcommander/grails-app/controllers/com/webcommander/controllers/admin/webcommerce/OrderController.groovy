package com.webcommander.controllers.admin.webcommerce
import com.webcommander.admin.*
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.CommonService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.models.*
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.CartManagerException
import com.webcommander.throwables.CartManagerExceptionWrap
import com.webcommander.util.AppUtil
import com.webcommander.util.TemplateMatcher
import com.webcommander.webcommerce.*
import grails.converters.JSON
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

class OrderController {
    AdministrationService administrationService
    OrderService orderService
    CommonService commonService
    ConfigService configService
    CustomerService customerService
    ProductService productService
    RoleService roleService

    def loadAppView() {
        Integer count = orderService.getOrderCount(params)
        params.max = params.max ?: "10";
        if(!session.super_vendor && !roleService.isPermitted(AppUtil.loggedOperator, new RestrictionPolicy(type: "order", permission: "view.list"), [:])) {
            params.operatorId = AppUtil.loggedOperator
        }
        List<Order> orders = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            orderService.getOrders(params)
        }
        render view: "/admin/order/appView", model: [count: count, orders: orders];
    }

    @Restriction(permission =  "order.create")
    def create() {
        List<PaymentGateway> paymentGateways = PaymentGateway.findAllByIsEnabled(true);
        render(view: "/admin/order/infoEdit", model: [paymentGateways: paymentGateways])
    }

    @Restriction(permission =  "order.create")
    def incompleteOrderPopupFromBackend() {
        if(params.popup != "true") {
            return ;
        }
        Long productId = params.long("productId")
        Integer quantity = params.int("quantity")
        Product product = Product.get(productId)
        ProductData productData = productService.getProductData(product, params.config)
        boolean requiresCombination = false
        if(productData.isCombined && productData.isCombinationQuantityFlexible) {
            requiresCombination = !params.included
        }
        def blockModel = HookManager.hook("productCartAdd", [blocks: []], productData, product, params)
        String popupTitle
        if(params.popupTitle) {
            popupTitle = params.popupTitle
        }
        else if((requiresCombination ? 1 : 0) + blockModel.blocks.size() > 1) {
            popupTitle = "information.required"
        } else if(requiresCombination) {
            popupTitle = "choose.quantity.included.products"
        } else {
            popupTitle = "choose.options"
        }
        Map model = [popupTitle: popupTitle, product: product, productData: productData, quantity: quantity, requiresCombination: requiresCombination]
        if(requiresCombination) {
            model.includedProducts = productService.getIncludedProducts([id: product.id])
        }
        blockModel.blocks.each {
            if(it.requiresKey) {
                model << [(it.requiresKey): true]
            }
            if(it.model) {
                model << it.model
            }
        }
        params.remove("controller")
        params.remove("action")
        model.quantity = model.quantity ?: model.productData.supportedMinOrderQuantity
        def html = g.include(view: "/site/incompleteDataCartPopup.gsp", model: model)
        render([status: "incomplete-info", html: html.toString()] as JSON)
    }

    @Restriction(permission =  "order.create")
    def save() {
        Map addresses = [:]
        Map products = params.products
        List items = []
        products.collect {
            if(!it.key.contains(".")) {
                items.add(it.value)
            }
        }
        Customer customer = customerService.getCustomer(params.long("customerId"))
        if(params.billingAddress) {
            addresses.billingAddress = orderService.getAddressFromJson(params.billingAddress)
            addresses.shippingAddress = orderService.getAddressFromJson(params.shippingAddress)
        } else {
            addresses.billingAddress = customer.activeBillingAddress
            addresses.shippingAddress = customer.activeShippingAddress
        }
        Cart cart;
        try {
            cart = CartManager.createCartForAdminOrder(items, addresses)
        } catch (CartManagerExceptionWrap ex) {
            CartManagerException cause = ex.exceptions[0]
            String error = g.message(code: cause.message.substring(2))
            def errorArgs = cause.messageArgs
            CartObject product = cause.product
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
            render([status: "error", message: error] as JSON);
            return;
        }
        if (cart.shippingCost.shipping == null) {
            render([status: "error", message: g.message(code: "not.support.shipping.choose.address")] as JSON)
            return;
        }
        def orderId
        Order.withNewTransaction {
            orderId = orderService.saveOrder(cart, addresses.billingAddress, addresses.shippingAddress, customer);
            Order order = Order.get(orderId);
            order.createdBy = Operator.get(AppUtil.loggedOperator);
            order.save();
            orderService.savePayment([
                orderId: orderId,
                paymentGateway: params.paymentGateway,
                amount: order.due,
                paymentStatus: NamedConstants.PAYMENT_STATUS.AWAITING
            ]);
        }
        if (orderId) {
            render([status: "success", message: g.message(code: "order.save.success")] as JSON)
            Thread.start {
                AppUtil.initialDummyRequest()
                orderService.sendEmailForOrder(orderId, "create-order");
            }
        } else {
            render([status: "error", message: g.message(code: "order.could.not.save")] as JSON)
        }
    }

    @Restriction(permission = "order.manage.order", entity_param = "id", domain = Order, owner_field = "createdBy")
    def print() {
        Order order = orderService.getOrder(params.long("id"))
        StoreDetail storeDetail = StoreDetail.first();
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ORDER_PRINT_AND_EMAIL)
        def orderItemShippedQuantity = orderService.getShipmentInfoForOrder(order);
        Map newConfig = [:]
        newConfig.putAll(config)
        if(params.view == "true") {
            newConfig.each {
                it.value = "true"
            }
            newConfig.subtotal_price = "without_tax_without_discount"
            newConfig.product_total_price = "without_tax_without_discount"
        }
        render(view: "/admin/order/printOrder", model: [
            order: order,
            storeDetail: storeDetail,
            shippedQuantity: orderItemShippedQuantity,
            config: newConfig
        ]);
    }

    @Restriction(permission = "order.view", entity_param = "id", domain = Order, owner_field = "createdBy")
    def view() {
        Long id = params.long("id");
        Order order = orderService.getOrder(id);
        render(view: "/admin/order/infoView", model: [order: order]);
    }

    def loadCustomer() {
        params.max = params.max ?: "10";
        params.status = DomainConstants.CUSTOMER_STATUS.ACTIVE
        Integer count = customerService.getCustomerCount(params)
        List<Customer> customers = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            customerService.getCustomers(params)
        }
        Customer newCustomer = params.newCustomer ?  Customer.get(params.newCustomer) : null;
        render view: "/admin/order/loadCustomer", model: [count: count, customers: customers, params: params, newCustomer: newCustomer];
    }

    def loadProduct() {
        params.stock = "in";
        params.parent = "all";
        Integer count = productService.getProductsCount(params)
        params.max = params.max ?: "10"
        List<Product> products = commonService.withOffset(params.max, params.offset, count) {max, offset, _count ->
            params.offset = offset
            productService.getProducts(params)
        }
        def productData = productService.getProductData(products.id, [:], true)
        render(view: "/admin/order/loadProduct", model: [count: count, products: productData])
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderId", domain = Product, owner_field = "createdBy")
    def shipmentInformation() {
        Long orderId = params.orderId.toLong()
        Order order = Order.get(orderId)
        Integer count = order.items.size();

        List items = orderService.getOrderItemsList(params, order)
        List shippedItemList
        params.shipmentList = order.shipments.id
        if (params.shipmentList) {
            shippedItemList = orderService.getShipmentItems(params);
        }
        def orderItemShippedQuantity = orderService.getShipmentInfoForOrder(order);

        render(view: "/admin/order/loadShipmentInformation", model: [
            shippedQuantity: orderItemShippedQuantity,
            orderItem: items,
            count: count,
            shipmentList : order.shipments,
            shipmentItems: shippedItemList
        ])
    }

    @Restriction(permission = "order.manage.shipment")
    def showShipmentHistory() {
        List<ShipmentChangeHistory> historyList = ShipmentChangeHistory.createCriteria().list {
            eq "shipment.id", params.long("shipmentId")
        }
        def labelMap = [:]
        historyList.collect {labelMap[it.uuid] = it.historyLabel}
        historyList = historyList.findAll() {it.uuid == labelMap.keySet().first()}
        render view: "/admin/order/shipmentHistory", model: [historyList: historyList, labelMap: labelMap]
    }

    @Restriction(permission = "order.manage.shipment")
    def reloadShipmentHistory() {
        List<ShipmentChangeHistory> historyList = ShipmentChangeHistory.createCriteria().list {
            eq "uuid", params.uuid
        }
        render view: "/admin/order/historyTable", model: [historyList: historyList]
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderId", domain = Product, owner_field = "createdBy")
    def shipmentDetails() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        Long orderId = params.orderId.toLong()
        Order order = Order.get(orderId)
        params.shipmentList = order.shipments.id;
        List<ShipmentItem> items = [];
        Integer count = 0
        if (params.shipmentList) {
            items = orderService.getShipmentItems(params);
            count = orderService.getShipmentItemCount(params);
        }
        render(view: "/admin/order/loadShipmentDetails", model: [
            shipmentItems: items,
            count: count
        ])
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "orderID", domain = Order, owner_field = "createdBy")
    def addShipment() {
        Order order = Order.get(params.orderId.toLong())
        def orderItem = OrderItem.where {
            eq("order", order)
            eq("isShippable", true)
        }.list()
        List shippedItems
        Shipment shipment = Shipment.get(params.shippingId)
        shippedItems = shipment? orderService.getShipmentItems(shipment) : []
        def orderItemShippedQuantity = orderService.getShipmentInfoForOrder(order);

        render(view: "/admin/order/addShipment", model: [items: orderItem, shippedQuantity: orderItemShippedQuantity, order: order.id, shipment: shipment, shippedItems: shippedItems, orderComment: order.orderComment?.content])
    }

    @Restriction(permission = "order.manage.shipment", entity_param = "order", domain = Order, owner_field = "createdBy")
    def saveShipment() {
        if(!params.remainingToShip.toLong()) {
            render([status: "alert", message: g.message(code: "all.products.are.shipped")] as JSON);
        }
        List quantity =  params.list("quantity").collect{ it == '' ? 0 : it.toLong()}
        if(!quantity.sum()) {
            render([status: "error", message: g.message(code: "no.product.is.selected.for.shipment")] as JSON)
        }
        if(orderService.saveShipment(params)) {
            render([status: "success", message: g.message(code: "shipment.save.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "shipment.save.error")] as JSON);
        }
    }
    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def sendInvoice() {
        List<Long>  ids = params.list("orderId")*.toLong();
        try {
            ids.each {
                orderService.sendEmailForOrder(it, "send-invoice")
            }
        } catch (e) {
            render([status: "error", message: g.message(code: "email.could.not.be.sent")] as JSON);
        }
        render([status: "success", message: g.message(code: "invoice.send.success")] as JSON)
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def managePayment() {
        Long orderId = params.orderId.toLong()
        Order order = Order.get(orderId)
        Integer count = order.payments.size()

        params.max = params.max ?: "10";
        List<Payment> payments = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max;
            params.offset = offset;
            orderService.getPaymentList(params, order)
        }

        render(view: "/admin/order/loadPaymentInfo", model: [
                payments: payments,
                order   : order,
                count   : count
        ])
    }

    def refundPayment() {
        Payment payment = Payment.get(params.id.toLong())
        if(orderService.refundPayment(payment)) {
            Boolean reAdjustInventory = false;
            Currency currency = AppUtil.baseCurrency
            def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_stock");
            if (payment.order.paid == 0.0 && config == DomainConstants.UPDATE_STOCK.AFTER_PAYMENT) {
                reAdjustInventory = true;
            }
            render([status: "success", message: g.message(code: "payment.refund.success", args: [currency.code + " " + currency.symbol + " " + payment.amount, payment.order.id]), reAdjustInventory: reAdjustInventory, orderId: payment.order.id, paymentId: payment.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "payment.refund.error")] as JSON)
        }
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def makePayment() {
        Long orderId = params.orderId.toLong()
        Payment payment = Payment.get(params.id ? params.id.toLong() : 0)
        List paymentGateways = PaymentGateway.findAllByCodeNotEqual(DomainConstants.PAYMENT_GATEWAY_CODE.API)
        render(view: "/admin/order/makePayment", model: [order: Order.get(orderId), payment: payment, paymentGateways: paymentGateways])
    }

    @Restriction(permission = "order.manage.payment", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def savePayment() {
        if(orderService.savePayment(params)) {
            render([status: "success", message: g.message(code: "payment.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "payment.save.error")] as JSON)
        }
    }

    @Restriction(permission = "order.manage.order")
    def changeStatus() {
        params.status = (params.status == "complete") ? "completed" : "cancelled"
        def result = orderService.changeOrderStatus(params);
        if(result) {
            render([status: "success", message: g.message(code: "order." + params.status + ".success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "order." + params.status + ".failure")] as JSON)
        }
    }

    def selectShippingClass() {
        render(view: "/admin/order/selectShippingClass");
    }

    def filter() {
        render(view: "/admin/order/filter")
    }

    def reAdjustInventory() {
        Long orderId = params.long("orderId");
        Long paymentId = params.long("paymentId");
        if (orderService.reAdjustInventory(orderId, paymentId)) {
            render([status: "success", message: g.message(code: "inventory.adjust.success")] as JSON)
        }else {
            render([status: "error", message: g.message(code: "inventory.adjust.failure")] as JSON)
        }
    }

    def changeOrderAddress() {
        Long orderId = params.orderId.toLong(0)
        String addressType = params.addressType
        Order order = Order.get(orderId)
        Address address
        if(addressType == "shipping") {
            address = order.shipping
        }
        else {
            address = order.billing
        }
        Long defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong();
        def states = administrationService.getStatesForCountry(address ? address.country.id : defaultCountryId)
        render(view: "/admin/order/changeOrderAddress", model: [ address : address, defaulCountryId: defaultCountryId, states: states])
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def updateOrderAddress() {
        if(orderService.saveOrderAddress(params)) {
            render([status: "success", message: g.message(code: "order.save.success"), id: params.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "order.save.failure")] as JSON)
        }
    }

    def changeAddressView() {
        render(view: "/admin/order/changeAddressPopup", model: [customerId: params.long("customerId")])
    }

    def loadAddressEditor() {
        Customer customer = Customer.get(params.long("customerId"))
        Address address
        if(params.section == "billing") {
            address = params.billingAddress ? orderService.getAddressFromJson(params.billingAddress) : customer.activeBillingAddress
        } else {
            address = params.shippingAddress ? orderService.getAddressFromJson(params.shippingAddress) : customer.activeShippingAddress
        }
        AddressData addressData = new AddressData(address)
        def states = administrationService.getStatesForCountry(addressData.countryId)
        Map fieldsConfigs = (Map) AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES["${params.section.toUpperCase()}_ADDRESS_FIELD"]);
        List sortedFields = configService.getSortedFields(fieldsConfigs)
        render(view: "/admin/order/editAddress", model: [fields: sortedFields, fieldsConfigs: fieldsConfigs, address: addressData,
                                                         states: states, section: params.section])
    }

    def priceAndUnavailableMsg() {
        Product product = Product.get(params.long("productId"))
        try {
            ProductData data = productService.getProductData(product)
            Double price = data.effectivePrice
            if(product.isCombined && params.included) {
                price = productService.getCombinationPrice(product, JSON.parse(params.included))
                if(price != null && price != data.basePrice) {
                    if(data.effectivePrice != data.basePrice) {
                        Double rate = data.effectivePrice / data.basePrice
                        price = rate * data.effectivePrice
                    }
                }
            }
            render([status: "success", price: price, currency: AppUtil.baseCurrency.symbol, productName: data.name] as JSON)
        } catch(ApplicationRuntimeException t) {
            render([status: "error"] as JSON)
        }
    }

    def validateQuantity() {
        Long productId = params.long("productId");
        Integer quantity = params.int("quantity");
        Long customerId = params.long("customerId");
        Map filterMap = [customer: customerId];
        Product product = productService.getProductIfAvailable(productId, filterMap);
        if(!product) {
            render([status: "alert", message: g.message(code: "product.not.available.for.customer")] as JSON)
            return;
        }
        Map addresses = [:]
        Map products = params.products
        List items = []
        products.collect {
            if(!it.key.contains(".")) {
                items.add(it.value)
            }
        }
        Cart cart = CartManager.createCartForAdminOrder(items, addresses)
        ProductData data = productService.getProductData(product, params.config)
        ProductInCart productInCart = new ProductInCart(data, params)
        try {
            List<CartItem> cartItems = cart.cartItemList.findAll {
                it.object.type == NamedConstants.CART_OBJECT_TYPES.PRODUCT && it.object.id == data.id && it.object.iEquals(data)
            }
            List variations = HookManager.hook("variationsForCartAdd", [], data, params.config)
            CartManager.populateCartItemForProduct(data, quantity, params, cartItems)
            render([status: "success"] as JSON)
        } catch (CartManagerException ex) {
            String error = g.message(code: ex.message.substring(2))
            def errorArgs = ex.messageArgs
            CartObject cartObject = ex.product
            if(errorArgs) {
                TemplateMatcher engine = new TemplateMatcher("%", "%")
                Map replacerMap = [
                        requested_quantity: errorArgs[0],
                        multiple_of_quantity: errorArgs[0],
                        maximum_quantity  : errorArgs[0],
                        minimum_quantity  : errorArgs[0],
                        product_name: cartObject.name
                ]
                error = engine.replace(error, replacerMap)
            }
            render([status: "error", message: error] as JSON);
        }
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def loadCommentView() {
        Long orderId = params.orderId ? params.orderId.toLong(0) : 0
        Order order = Order.get(orderId)
        StoreDetail storeDetail = StoreDetail.first()
        List<OrderComment> comments = OrderComment.findAllByOrder(order)
        render(view: "/admin/order/commentHistory", model: [order: order, storeDetail: storeDetail, comments: comments])
    }

    @Restriction(permission = "order.manage.order", entity_param = "orderId", domain = Order, owner_field = "createdBy")
    def saveComment() {
        Order order = Order.get(params.orderId.toLong(0))
        params.message = params.message.trim()
        StoreDetail storeDetail = StoreDetail.first()
        Address billingAddress = order.billing
        params.email = billingAddress.email
        OrderComment comment = orderService.sendOderComment(order, params)
        if(!comment.hasErrors()) {
            render([status: "success", msg: params.message.encodeAsBMHTML(), storeName: storeDetail.name, date: comment.created.toAdminFormat(true, false, session.timezone)] as JSON)
        } else {
            render([status: "error", message: g.message(code: "send.comment.fail")] as JSON)
        }
    }

    def exportOrder() {
        params.max = -1
        params.offset = 0
        List<Order> orders = orderService.getOrders(params)
        CsvListWriter listWriter = null
        try {
            response.setHeader("Content-Type", "text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
            OutputStreamWriter writer = new OutputStreamWriter(response.outputStream)
            listWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE)
            listWriter.writeHeader(
                "Order ID",
                "Customer Name",
                "Order Date",
                "Order total",
                "Order Status",
                "Shipment Status",
                "Payment Status",
                "Address Line 1",
                "Address Line 2",
                "City",
                "Post Code",
                "State",
                "Phone",
                "Mobile",
                "Country",
                "Products"
            )
            orders.each { Order order ->
                Address billing = order.billing
                listWriter.write([
                    order.id,
                    order.customerName,
                    order.created.toAdminFormat(true, false, session.timezone),
                    order.grandTotal.toPrice(),
                    order.orderStatus,
                    order.shippingStatus,
                    order.paymentStatus,
                    billing.addressLine1,
                    billing.addressLine2 ?: "",
                    billing.city ?: "",
                    billing.postCode ?: "",
                    billing.state?.name ?: "",
                    billing.phone ?: "",
                    billing.mobile ?: "",
                    billing.country.name,
                    order.items.productName.join(",")
                ])
            }

        } finally {
            if( listWriter != null ) {
                listWriter.close();
            }
        }
    }
}

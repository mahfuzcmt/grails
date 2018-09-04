package com.webcommander.controllers.admin.content

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.Document
import com.webcommander.content.DocumentService
import com.webcommander.manager.HookManager
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.webcommerce.Currency
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderComment
import com.webcommander.webcommerce.OrderService
import com.webcommander.webcommerce.PaymentGateway
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.Shipment
import com.webcommander.webcommerce.ShipmentItem
import grails.converters.JSON
import grails.web.databinding.DataBindingUtils

class DocumentController {
    CommonService commonService
    DocumentService documentService
    CommanderMailService commanderMailService
    OrderService orderService

    @Restriction(permission = "document.view.list")
    def loadAppView() {
        params.max = params.max ?: "10"
        Integer count = documentService.getDocumentCount(params)
        List<Currency> documents = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.max = max
            params.offset = offset
            documentService.getDocuments(params)
        }
        render(view: "/admin/document/appView", model: [documents: documents, count: count])
    }

    @Restrictions([
        @Restriction(permission = "document.create", params_not_exist = "id"),
        @Restriction(permission = "document.edit", params_exist = "id")
    ])
    def editDocument() {
        List<Document> layouts = documentService.getLayouts()
        render(view: "/admin/document/editorInitial", model: [layouts: layouts])
    }

    @Restrictions([
        @Restriction(permission = "document.create", params_not_exist = "id"),
        @Restriction(permission = "document.edit", params_exist = "id")
    ])
    def renderEditor() {
        Document document = Document.load(params.id)
        document = document ?: new Document(type: params.type)
        Document copyLayout = new Document()
        if(params.layoutUsed.toBoolean()) {
            Map documentMap = [name: document.name.concat(" Copy"), isLayout: document.isLayout, active: document.active, type: document.type, description: document.description, content: document.content]
            DataBindingUtils.bindObjectToInstance(copyLayout, documentMap,[], ['id', 'created', 'updated'], null)
        }
        if(!document.id) {
            document.discard()
        }
        def macros = NamedConstants.DOCUMENT_MACROS[document.type]
        render view: "/admin/document/editorView", model: [document: params.layoutUsed.toBoolean() ? copyLayout : document, macrosList: macros]
    }

    def loadMacroList() {
        String type = params.type ?: Document.load(params.id).type
        def macros = NamedConstants.DOCUMENT_MACROS[type]
        render view: "/admin/document/macroList", model: [macrosList: macros]
    }

    def getDocumentData() {
        def type = params.type
        def orderId = params.id
        Map attachment = [:]
        Document document = Document.findByTypeAndActive(type, true)
        if(!document) {
            return render(g.message(code: "no.active.document.found.for.${type}"))
        }
        Order.withNewSession {
            Order order = Order.get(orderId)
            def shipmentInfo = orderService.getShipmentInfoForOrder(order);
            def macrosType = ["billing_address":"", "customer_name":"", "shipping_address":"", "payment_info":"", "order_details":"", "shipping_details":"", "order_date":"", "order_id":"", "payment_details":"", "store_details":"", "order_comment":""]
            Map refinedMacros = commanderMailService.getCommonMacros()
            order.attach()
            macrosType.each {
                switch (it.key.toString()) {
                    case "order_details":
                        Map orderDetails = [:]
                        orderDetails.items = [];
                        int i=0
                        order.items.each {
                            Map item = [:];
                            item.product_name = it.productName.encodeAsBMHTML();
                            String url;
                            String itemNumber = null
                            if(it.productType == NamedConstants.CART_OBJECT_TYPES.PRODUCT) {
                                Product product = Product.get(it.productId);
                                itemNumber = product?.sku
                                url = product ? app.baseUrl() + "product/" + product.url : "";
                                if(product?.productType == DomainConstants.PRODUCT_TYPE.DOWNLOADABLE && order.paymentStatus == DomainConstants.ORDER_PAYMENT_STATUS.PAID) {
                                    item.product_download_url = app.baseUrl() + "page/downloadProduct?token=" + productService.getProductFileDownloadToken(it)
                                    item.is_downloadable =  true
                                } else {
                                    item.isDownloadable =  false
                                }
                            }

                            def delivered= shipmentInfo[i] == null ? 0 : shipmentInfo[i].deliveredQuantity
                            def undelivered = it.quantity - delivered
                            def quantityInStock = Product.get(it.productId).availableStock
                            def backOrdered = undelivered > quantityInStock ? undelivered - quantityInStock : 0
                            def quantityPicked = quantityInStock > undelivered ? undelivered : quantityInStock

                            item.back_ordered = backOrdered
                            item.pick_task = quantityPicked.encodeAsBMHTML();
                            item.quantity_shipped = delivered.encodeAsBMHTML();
                            item.sku = itemNumber
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
                            orderDetails.items.add(item);
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
                        StringBuilder payingDate = new StringBuilder()
                        order.payments.each {payingDate << it.payingDate.toFormattedString("YYYY-MM-dd", false, null, null, null) + ", "}
                        StringBuilder paymentMethod = new StringBuilder()
                        order.payments.each {paymentMethod << g.message(code: (PaymentGateway.findByCode(it.gatewayCode).name)) + ", "}
                        StringBuilder trackInfo = new StringBuilder()
                        order.payments.each {trackInfo << it.trackInfo + ", "}
                        def payerInfo = order.payments.each {it.payerInfo + ", "}
                        StringBuilder status = new StringBuilder()
                        order.payments.each {status << g.message(code: it.status) + ", "}
                        StringBuilder amount = new StringBuilder()
                        order.payments.each {amount << it.amount.toPrice() + ", "}
                        refinedMacros[it.key] = [
                                date: payingDate.toString() ?: "",
                                payment_method: paymentMethod.toString() ?: "",
                                track_info: trackInfo.toString() ?: "",
                                payer_info: payerInfo.toString() ?: "",
                                status: status.toString() ?: "",
                                amount: amount.toString() ?: "",
                        ]
                        break;
                    case "billing_address":
                        refinedMacros[it.key] = [
                                customer_name: order.billing.firstName ? (order.billing.firstName.encodeAsBMHTML() + " " + order.billing.lastName.encodeAsBMHTML()) : "",
                                address_line1: order.billing.addressLine1.encodeAsBMHTML() ?: "",
                                state: order.billing.state?.name ?: "",
                                city: order.billing.city ?: "",
                                post_code: order.billing.postCode ?: "",
                                email: order.billing.email ?: "",
                                phone: order.billing.phone ?: "",
                                mobile: order.billing.mobile ?: "",
                                fax: order.billing.fax ?: "",
                                country: order.billing.country.name ?: ""
                        ]
                        break;
                    case "shipping_address":
                        def shipping_address_map = [:]
                        if(order.shipping) {
                            shipping_address_map = [
                                    customer_name: order.shipping.firstName ? (order.shipping.firstName.encodeAsBMHTML() + " " + order.shipping.lastName.encodeAsBMHTML()) : "",
                                    address_line1: order.shipping.addressLine1.encodeAsBMHTML() ?: "",
                                    state: order.shipping.state?.name ?: "",
                                    city: order.shipping.city ?: "",
                                    post_code: order.shipping.postCode ?: "",
                                    email: order.shipping.email ?: "",
                                    phone: order.shipping.phone ?: "",
                                    mobile: order.shipping.mobile ?: "",
                                    fax: order.shipping.fax ?: "",
                                    country: order.shipping.country.name ?: "",
                            ]
                        } else {
                            shipping_address_map = [
                                    customer_name: order.shipping.firstName ? (order.shipping.firstName.encodeAsBMHTML() + " " + order.shipping.lastName.encodeAsBMHTML()) : "",
                                    address_line1: order.shipping.addressLine1.encodeAsBMHTML() ?: "",
                                    state: order.shipping.state?.name ?: "",
                                    city: order.shipping.city ?: "",
                                    post_code: order.shipping.postCode ?: "",
                                    email: order.shipping.email ?: "",
                                    phone: order.shipping.phone ?: "",
                                    mobile: order.shipping.mobile ?: "",
                                    fax: order.shipping.fax ?: "",
                                    country: order.shipping.country.name ?: ""
                            ]
                        }
                        refinedMacros[it.key] = shipping_address_map
                        break;
                    case "shipping_details":
                        List shipmentDetails = []
                        order.shipments.each { Shipment shipment ->
                            shipment.shipmentItem.each { ShipmentItem item ->
                                Map entry = [:];
                                entry.product_name = item.orderItem.productName.encodeAsBMHTML();
                                entry.order_quantity = item.orderItem.quantity;
                                entry.shipment_method = g.message( code: shipment.method);
                                entry.shipped_quantity = item.quantity;
                                entry.track_info = shipment.trackingInfo;
                                shipmentDetails.add(entry);
                            }
                        }
                        refinedMacros[it.key] = shipmentDetails;
                        break;
                    case "order_id":
                        refinedMacros[it.key] = order.id ?: "";
                        break;
                    case "order_date":
                        refinedMacros[it.key] = order.created.toFormattedString("YYYY-MM-dd", false, null, null, null) ?: "";
                        break;
                    case "customer_name":
                        refinedMacros[it.key] = order.customerName ?: "";
                        break;
                    case "customer_login_url":
                        refinedMacros[it.key] = refinedMacros["store_url"] + "customer/login";
                        break;
                    case "payment_info":
                        String paymentInfo = order.payments ? PaymentGateway.findByCode(order.payments.last().gatewayCode).information : "";
                        refinedMacros[it.key] = paymentInfo
                        break
                    case "currency_symbol":
                        refinedMacros[it.key] = AppUtil.baseCurrency.symbol;
                    case "order_comment":
                        List<OrderComment> comments = OrderComment.findAllByOrderAndIsVisibleToCustomer(order, true)
                        StringBuilder commentString = new StringBuilder()
                        comments.each {comment ->
                            String name = comment.isAdmin ? comment.adminName.encodeAsBMHTML() : (order.customerName ? order.customerName.encodeAsBMHTML() : 'customer')
                            commentString << name
                            commentString << "<br>"
                            commentString << comment.created.toSiteFormat(true, false, session.timezone)
                            commentString << "<br>"
                            commentString << comment.content.encodeAsBMHTML()
                            commentString << "<br>"
                            commentString << "<br>"
                        }
                        println(commentString.toString())
                        refinedMacros[it.key] = commentString
                }
            }
            Map macros = new LinkedHashMap(refinedMacros)

            attachment = documentService.getPdfData(type, type + "_" + orderId, macros)

            render(view: "/admin/document/print", model: [data: attachment.byte.encodeAsBase64()])
        }
    }


    def isUnique() {
        render(commonService.responseForUniqueField(Document, params.long("id"), params.field, params.value) as JSON)
    }

    @Restrictions([
        @Restriction(permission = "document.create", params_not_exist = "id"),
        @Restriction(permission = "document.edit", params_exist = "id")
    ])
    def save() {
        try {
            def document = documentService.save(params)
            render([status: "success", message: "document.save.success", id: document.id] as JSON)
        } catch (Exception e)  {
            e.printStackTrace()
            render([status: "error", message: "document.save.error"] as JSON)
        }
    }

    def componentConfig() {
        Map config = [:]
        switch (params.type) {
            case "invoice_table":
                params.each {
                    if(it.key.startsWith("column-")) {
                        String key = it.key.split("column-")[1]
                        config[key] = params["value-" + key]
                    }
                }
                break
            case "order_table":
                params.each {
                    if(it.key.startsWith("column-")) {
                        String key = it.key.split("column-")[1]
                        config[key] = params["value-" + key]
                    }
                }
                break
            case "delivery_docket_table":
                params.each {
                    if(it.key.startsWith("column-")) {
                        String key = it.key.split("column-")[1]
                        config[key] = params["value-" + key]
                    }
                }
                break
            case "picking_slip_table":
                params.each {
                    if(it.key.startsWith("column-")) {
                        String key = it.key.split("column-")[1]
                        config[key] = params["value-" + key]
                    }
                }
                break
        }
        render(view: "/admin/document/componentConfig/${params.type}", model: [config: config])
    }

    def delete() {
        Long id = params.long("id")
        try {
            if (documentService.delete(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "document.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "document.delete.failure")] as JSON)
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def deleteSelected() {
        List ids = params.list("ids").collect{ it.toLong() }
        Boolean result = documentService.deleteSelected(ids)
        if(result) {
            render([status: "success", message: g.message(code: "document.delete.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "document.delete.failure")] as JSON);
        }
    }

    def copy() {
        try {
            documentService.copy(params)
            render([status: "success", message: g.message(code: "document.copy.success")] as JSON)
        } catch (Exception e) {
            e.printStackTrace()
            render([status: "error", message: g.message(code: "document.copy.failure")] as JSON)
        }
    }

    def setActiveTemplate() {
        Boolean result = documentService.setActivetemplate(params)
        if(result) {
            render([status: "success", message: g.message(code: "document.update.success")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "document.update.failure")] as JSON);
        }
    }

    def isDocumentActive() {
        Document document = Document.findByTypeAndActive(params.type, true)
        render([status: document ? "success" : "error"] as JSON)
    }
}
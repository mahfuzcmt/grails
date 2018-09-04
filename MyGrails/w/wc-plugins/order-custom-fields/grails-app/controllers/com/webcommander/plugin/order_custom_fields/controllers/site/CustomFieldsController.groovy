package com.webcommander.plugin.order_custom_fields.controllers.site

import com.webcommander.manager.CartManager
import com.webcommander.plugin.order_custom_fields.OrderCheckoutFields
import com.webcommander.plugin.order_custom_fields.OrderCustomFieldsService
import grails.converters.JSON

class CustomFieldsController {

    OrderCustomFieldsService orderCustomFieldService

    def loadCustomFieldsStep() {
        List<OrderCheckoutFields> fields = OrderCheckoutFields.list()
        render(view: "/plugins/order_custom_fields/customFieldsBlock", model: [fields: fields])
    }

    def holdFields() {
        if(!CartManager.hasCart(session.id)) {
            render(text: g.message(code: "cart.not.available"), status: 412)
            return;
        }
        session.order_custom_fields = params.custom
        render([status: "success"] as JSON)
    }
}
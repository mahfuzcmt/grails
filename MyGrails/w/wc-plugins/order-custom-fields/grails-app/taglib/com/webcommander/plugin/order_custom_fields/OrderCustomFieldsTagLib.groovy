package com.webcommander.plugin.order_custom_fields

import com.webcommander.constants.NamedConstants
import com.webcommander.plugin.order_custom_fields.OrderCheckoutFields
import com.webcommander.plugin.order_custom_fields.OrderCustomFieldsService
import com.webcommander.plugin.order_custom_fields.OrderCheckoutFieldsTitle
import com.webcommander.plugin.order_custom_fields.OrderCustomData
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class OrderCustomFieldsTagLib {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g
    OrderCustomFieldsService orderCustomFieldsService

    static namespace = "orderFields"

    def adminJSs = { attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/order-custom-fields/js/custom-fields.js')
    }

    def customFields = { attrs, body ->
        String title = ""
        if (OrderCheckoutFieldsTitle.count() > 0) {
            title = OrderCheckoutFieldsTitle.list().get(0).title
        }
        if(OrderCheckoutFields.count() > 0) {
            String clazz = pageScope.step == pageScope.nextStep ? 'expanded' : 'collapsed'
            out << "<div class='label-bar ${clazz}' item-url='customFields/loadCustomFieldsStep'><a class='toggle-icon'></a> ${title.encodeAsBMHTML()}</div><div class='custom-field accordion-item ${clazz}' event_name='custom-field-change' step_index='${pageScope.nextStep++}' step='${NamedConstants.CHECKOUT_PAGE_STEP.ORDER_CUSTOM_FIELDS}'></div>"
        }
        out << body()
    }

    def loadDetailsView = { attrs, body ->
        out << body()
        List<OrderCustomData> customData = OrderCustomData.where {
            eq("order.id", (pageScope.order).id)
        }.list()
        out << g.include(view: "plugins/order_custom_fields/viewDetails.gsp", model: [customData: customData])
    }
}
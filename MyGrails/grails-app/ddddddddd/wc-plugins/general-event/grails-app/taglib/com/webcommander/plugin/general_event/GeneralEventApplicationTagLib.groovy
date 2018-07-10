package com.webcommander.plugin.general_event

import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.plugin.general_event.model.CartGeneralEventTicket
import com.webcommander.plugin.general_event.model.CartRecurringEventTicket

class GeneralEventApplicationTagLib {

    GeneralEventService generalEventService

    static namespace = "generalEventApp"

    def adminJSs = {attrs, body ->
        out << body()
        out << app.javascript(src: 'plugins/general-event/fullcalendar/fullcalendar.min.js')
    }

    def adminCSSs = {attrs, body ->
        out << app.stylesheet(href: "plugins/general-event/fullcalendar/fullcalendar.css")
    }

    def decimalToAlphabet = { attrs, body ->
        out << generalEventService.decimalToAlphabet(attrs.number)
    }

    def eCommerceSettings = { Map attrs, body ->
        out << body()
        out << g.include(view: "/plugins/general_event/admin/eCommerceSettings.gsp", model: [config: attrs.config] )
    }

    def eventCustomField = { Map attr, body ->
        Cart cart = CartManager.getCart(session.id);
        Map model = [:]
        for(CartItem item : cart.cartItemList) {
            def cartObject = item.object
            if(cartObject instanceof CartGeneralEventTicket || cartObject instanceof CartRecurringEventTicket) {
                Long eventId = cartObject instanceof CartGeneralEventTicket ? cartObject.eventId : cartObject.parentEventId
                String title = generalEventService.getFieldsOrTitle(eventId, true);
                if(generalEventService.getFieldsOrTitle(eventId).fields) {
                    int quantity = item.quantity;
                    String clazz = pageScope.step == pageScope.nextStep ? 'expanded' : 'collapsed';
                    out << "<div class='label-bar ${clazz}' item-url='generalEvent/loadCustomFieldsStep?eventId=${eventId}&quantity=${quantity}'><a class='toggle-icon'></a> " +
                            "${title ?: GeneralEvent.proxy(eventId).name}</div><div class='custom-field accordion-item ${clazz}' event_name='custom-field-change' step_index='${pageScope.nextStep++}' step='custom'></div>"
                }
            }
        }
        out << body()
    }
}

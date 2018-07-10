package com.webcommander.plugin.simplified_event_management

import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartObject
import com.webcommander.plugin.simplified_event_management.model.CartSimplifiedEventTicket
import com.webcommander.plugin.simplified_event_management.webmarketing.SimplifiedEventService

/**
 * Created by arman on 11/11/2015.
 */
class SimplifiedEventManagementApplicationTagLib {
    SimplifiedEventService simplifiedEventService

    static namespace = "simplifiedEventManagementApp"

    def adminJSs = {attrs, body ->
        out << body()
        def url = "plugins/simplified-event-management/js/tabs";
        out << app.javascript(src: "$url/simplified-event-tab.event.js")
        out << app.javascript(src: "$url/simplified-event-tab.calendar.js")
        out << app.javascript(src: "$url/simplified-event-tab.custom-field.js")
        out << app.javascript(src: "$url/simplified-event-tab.custom-field-data.js")
        out << app.javascript(src: "plugins/simplified-event-management/fullcalendar/fullcalendar.min.js")
    }

    def adminCSSs = {attrs, body ->
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${app.systemResourceBaseUrl()}plugins/simplified-event-management/fullcalendar/fullcalendar.css\">"
    }

    def decimalToAlphabet = { attrs, body ->
        out << simplifiedEventService.decimalToAlphabet(attrs.number)
    }

    def eCommerceSettings = { Map attrs, body ->
        out << body()
        out << g.include(view: "/plugins/simplified_event_management/admin/eCommerceSettings.gsp", model: [config: attrs.config] )
    }

    def eventCustomField = { Map attr, body ->
        Cart cart = CartManager.getCart(session.id);
        Map model = [:]
        for(int i = 0; i < cart.cartItemList.size(); i++) {
            if(cart.cartItemList[i].object instanceof CartSimplifiedEventTicket) {
                Long id = cart.cartItemList[i].object.id.toLong();
                String title = simplifiedEventService.getFieldsOrTitle(id, true);
                if(simplifiedEventService.getFieldsOrTitle(id).fields) {
                    int quantity = cart.cartItemList[i].quantity;
                    String clazz = pageScope.step == pageScope.nextStep ? 'expanded' : 'collapsed';
                    out << "<div class='label-bar ${clazz}' item-url='simplifiedEvent/loadCustomFieldsStep?eventId=${id}&quantity=${quantity}'><a class='toggle-icon'></a> ${title?: SimplifiedEvent.proxy(id).name}</div><div class='custom-field accordion-item ${clazz}' event_name='custom-field-change' step_index='${pageScope.nextStep++}' step='custom'></div>"
                }
            }
        }
        out << body()
    }
}

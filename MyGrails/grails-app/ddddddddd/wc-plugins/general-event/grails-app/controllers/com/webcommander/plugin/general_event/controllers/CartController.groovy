package com.webcommander.plugin.general_event.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.GeneralEventService
import com.webcommander.plugin.general_event.RecurringEvents
import com.webcommander.plugin.general_event.VenueLocationSection
import com.webcommander.plugin.general_event.manager.VenueTicketCartManager
import com.webcommander.plugin.general_event.model.CartGeneralEventTicket
import com.webcommander.plugin.general_event.model.CartRecurringEventTicket
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.TemplateMatcher
import org.apache.commons.lang.StringUtils

class CartController {

    GeneralEventService generalEventService

    @License(required = "allow_general_event_feature")
    def loadCartTicketPopup() {
        def event = params.isRecurring.toBoolean() ? RecurringEvents.get(params.long("eventId")) : GeneralEvent.get(params.long("eventId"))
        VenueLocationSection section = VenueLocationSection.proxy(params.sectionId);
        Set lockedTickets = generalEventService.getLockedTickets(section, event);
        String errorCode;
        int availableTickets = 0;
        Integer ordered = params.int("orderedQuantity");
        if(section.rowCount * section.columnCount < lockedTickets.size() + ordered) {
            errorCode = "quantity.ticket.not.available"
            availableTickets = (section.rowCount * section.columnCount) - lockedTickets.size()
        }
        Integer rowNumber = generalEventService.getStartRowNumber(section)
        Integer columnNumber = generalEventService.getStartColumnNumber(section)
        render(view: "/plugins/general_event/site/ticketAddToCartPopup", model: [orderedQuantity: ordered, availableTickets: availableTickets, event: params.eventId.toLong(), errorCode: errorCode,
                                                                                    section: section, lockedTickets: lockedTickets, rowNumber: rowNumber, columnNumber: columnNumber])
    }

    synchronized
    @License(required = "allow_general_event_feature")
    def addVenueTicket() {
        VenueLocationSection section
        def event
        def ticket
        boolean success;
        String error
        CartItem item
        List errorArgs
        Cart cart
        def seats = params.list("seats").collect {it.toInteger()}
        section = VenueLocationSection.get(params.sectionId.toLong())
        Boolean isRecurring = params.isRecurring.toBoolean()
        event = isRecurring ? RecurringEvents.get(params.long("eventId")) : GeneralEvent.get(params.long("eventId"))
        if(seats.find {
            boolean available = generalEventService.isTicketAvailable(it, event, section)
            if(!available) {
                return true
            }
        }){
            error = "selected.seat.not.available"
        } else {
            if(isRecurring) {
                ticket = new CartRecurringEventTicket(seats as Integer[], section.id, event?.id)
            }else {
                ticket = new CartGeneralEventTicket(seats as Integer[], section.id, event?.id)
            }
            try {
                item = CartManager.addToCart(session.id, ticket, seats.size())
                String seatList = StringUtils.join(seats, ',')
                item.setIsQuantityAdjustable(false)
                item.variations = ["Seat: " + generalEventService.seatNumberToName(section, seats).join(", ") + ' #' + seatList + ' #' + event.id]
                VenueTicketCartManager.addedInCart(item)
                success = true;
            } catch(CartManagerException ex) {
                error = ex.message;
                success = false;
                item = new CartItem(ex.product, 0)
                errorArgs = ex.messageArgs
                error = site.message(code: error)
                if(errorArgs) {
                    TemplateMatcher engine = new TemplateMatcher("%", "%")
                    Map replacerMap = [
                            event_name: errorArgs[0],
                            requested_quantity: errorArgs[0],
                            multiple_of_quantity: errorArgs[0],
                            maximum_quantity: errorArgs[0],
                            minimum_quantity: errorArgs[0]
                    ]
                    error = engine.replace(error, replacerMap)
                }
            }
            cart = CartManager.getCart(session.id);
        }
        render(view: "/site/addToCartPopup", model: [success: success, cartdata: item, quantity: seats.size(), errorMessage: error, cart: cart, totalItem: cart ? cart.cartItemList.size() : 0,
                                                     errorArgs: errorArgs])
    }

    @License(required = "allow_general_event_feature")
    def addTicketToCart() {
        def ticket
        boolean success;
        String error
        CartItem item
        List errorArgs
        Cart cart
        Integer seatQuantity = params.("seats").toLong();
        if(params.isRecurring.toBoolean()) {
            ticket = new CartRecurringEventTicket(params.eventId.toLong())
        }else {
            ticket = new CartGeneralEventTicket(params.eventId.toLong());
        }
        try {
            item = CartManager.addToCart(session.id, ticket, seatQuantity)
            success = true;
        } catch(CartManagerException ex) {
            error = ex.message;
            success = false;
            item = new CartItem(ex.product, 0)
            errorArgs = ex.messageArgs
            error = site.message(code: error)
            if(errorArgs) {
                TemplateMatcher engine = new TemplateMatcher("%", "%")
                Map replacerMap = [
                        event_name: errorArgs[0],
                        requested_quantity: errorArgs[0],
                        multiple_of_quantity: errorArgs[0],
                        maximum_quantity: errorArgs[0],
                        minimum_quantity: errorArgs[0]
                ]
                error = engine.replace(error, replacerMap)
            }
        }
        cart = CartManager.getCart(session.id);
        render(view: "/site/addToCartPopup", model: [success: success, cartdata: item, quantity: seatQuantity, errorMessage: error, cart: cart, totalItem: cart ? cart.cartItemList.size() : 0,
                                                     errorArgs: errorArgs])
    }
}

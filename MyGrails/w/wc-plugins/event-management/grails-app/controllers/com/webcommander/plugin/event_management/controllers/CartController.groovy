package com.webcommander.plugin.event_management.controllers

import com.webcommander.authentication.annotations.License
import com.webcommander.manager.CartManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.EventSession
import com.webcommander.plugin.event_management.VenueLocationSection
import com.webcommander.plugin.event_management.manager.CartTicketManager
import com.webcommander.plugin.event_management.model.CartEventTicket
import com.webcommander.plugin.event_management.webmarketing.EventService
import com.webcommander.throwables.CartManagerException

class CartController {
    EventService eventService

    @License(required = "allow_event_feature")
    def loadCartPopup() {
        VenueLocationSection section = VenueLocationSection.proxy(params.section)
        Set lockedTickets = eventService.getLockedTickets(section, Event.proxy(params.event), EventSession.proxy(params.session))
        String errorCode;
        int availableTickets = 0;
        Integer ordered = params.int("orderedQuantity")
        if(section.rowCount * section.columnCount < lockedTickets.size() + ordered) {
            errorCode = "quantity.ticket.not.available"
            availableTickets = (section.rowCount * section.columnCount) - lockedTickets.size()
        }
        Integer rowNumber = eventService.getStartRowNumber(section)
        Integer columnNumber = eventService.getStartColumnNumber(section)
        render(view: "/plugins/event_management/site/ticketAddToCartPopup", model: [orderedQuantity: ordered, availableTickets: availableTickets, event: params.event.toLong(), session: params.session.toLong(), errorCode: errorCode,
                                                                                    section: section, lockedTickets: lockedTickets, rowNumber: rowNumber, columnNumber: columnNumber])
    }

    synchronized
    @License(required = "allow_event_feature")
    def addTicket() {
        def seats = params.list("seats").collect {it.toInteger()}
        VenueLocationSection section = VenueLocationSection.get(params.section.toLong())
        Event event = Event.get(params.event.toLong())
        EventSession esession = EventSession.get(params.session.toLong())
        boolean success;
        String error
        CartItem item
        Map errorArgs
        Cart cart
        if(seats.find {
            boolean available = eventService.isTicketAvailable(it, event?.id, esession?.id, section.id)
            if(!available) {
                return true
            }
        }) {
            error = "selected.seat.not.available"
        } else {
            CartEventTicket ticket = new CartEventTicket(seats as Integer[], section.id, event?.id, esession?.id)
            try {
                item = CartManager.addToCart(session.id, ticket, seats.size())
                item.variations = ["Seat: " + eventService.seatNumberToName(section, seats).join(", ")]
                CartTicketManager.addedInCart(item)
                success = true;
            } catch(CartManagerException ex) {
                error = ex.message;
                success = false;
                item = new CartItem(ex.product, 0)
                errorArgs = ex.messageArgs
            }
            cart = CartManager.getCart(session.id);
        }
        render(view: "/site/addToCartPopup", model: [success: success, cartdata: item, quantity: seats.size(), error: error, cart: cart, totalItem: cart ? cart.cartItemList.size() : 0,
                                                     errorArgs: errorArgs])
    }

}

package com.webcommander.plugin.general_event.model

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.QuantityAdjustableCartObject
import com.webcommander.models.TaxableCartObject
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.VenueLocationSection
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxProfile

/**
 * Created by arman on 1/6/2016.
 */

class CartGeneralEventTicket implements TaxableCartObject, QuantityAdjustableCartObject {

    Long eventId;
    Long sectionId;
    Integer[] seats

    String type = NamedConstants.CART_OBJECT_TYPES.GENERAL_EVENT_TICKET

    CartGeneralEventTicket(Long eventId) {
        id = this.eventId =eventId
        GeneralEvent event = GeneralEvent.get(id)
        name = "Ticket For " + event.name
        image = event.images ? event.images[0].name : null
        refresh()
    }

    CartGeneralEventTicket(Integer[] seats, Long sectionId, Long eventId) {
        type = NamedConstants.CART_OBJECT_TYPES.GENERAL_EVENT_VENUE_TICKET
        this.eventId = eventId;
        this.sectionId = sectionId;
        this.seats = seats;
        id = sectionId
        GeneralEvent _event = GeneralEvent.get(eventId)
        VenueLocationSection _section = VenueLocationSection.get(sectionId)
        name = "Ticket For " + _event.name + " (" + _section.name + ")"
        image = _event.images ? _event.images[0].name : null
        refresh()
    }

    @Override
    void validate(Integer quantity) {
        GeneralEvent event = GeneralEvent.get(eventId)
        Integer available = event.maxTicket - event.totalSoldTicket;
        if(event.endDateTime.gmt(AppUtil.session.timezone) < new Date().gmt(AppUtil.session.timezone)) {
            throw new CartManagerException(this, "s:your.requested.event.date.expired", [event.name]);
        }
        if(quantity > available) {
            throw new CartManagerException(this, "s:requested.quantity.ticket.not.available", [quantity])
        }
        if (event.maxTicketPerCustomer && quantity > event.maxTicketPerCustomer) {
            throw new CartManagerException(this,  "s:you.can.buy.maximum.quantity.for.event", [event.maxTicketPerCustomer])
        }
    }

    @Override
    Integer available(Integer quantity) {
        GeneralEvent event = GeneralEvent.get(eventId)
        Integer available = event.maxTicket - event.totalSoldTicket;
        if(event.maxTicketPerCustomer && available > event.maxTicketPerCustomer) {
            available = event.maxTicketPerCustomer
        }
        return available
    }

    @Override
    void refresh() {
        effectivePrice = sectionId ? VenueLocationSection.get(sectionId).ticketPrice : GeneralEvent.get(eventId).ticketPrice
    }

    @Override
    String getLink() {
        return 'generalEvent/' + eventId + '?isRecurring=false';
    }

    @Override
    String getImageLink(String imageSize) {
        GeneralEvent event = GeneralEvent.get(eventId)
        int size = 100;
        if(imageSize.toInteger() > size) {
            size = 300
        }
        return 'resources/general-event/' + (event.images ? 'event-' + event.id + "/images/$size-" + event.images[0].name : "default/$size-default.png");
    }

    @Override
    TaxProfile resolveTaxProfile() {
        GeneralEvent event = GeneralEvent.get(eventId)
        TaxProfile profile = event.taxProfile
        if(!profile) {
            profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile").toLong(0));
        }
        return profile;
    }
}

package com.webcommander.plugin.general_event.model

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.QuantityAdjustableCartObject
import com.webcommander.models.TaxableCartObject
import com.webcommander.plugin.general_event.GeneralEvent
import com.webcommander.plugin.general_event.RecurringEvents
import com.webcommander.plugin.general_event.VenueLocationSection
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxProfile

/**
 * Created by arman on 1/11/2016.
 */

class CartRecurringEventTicket implements TaxableCartObject, QuantityAdjustableCartObject {

    Long eventId
    Long parentEventId
    Long sectionId
    Integer[] seats

    String type = NamedConstants.CART_OBJECT_TYPES.RECURRING_EVENT_TICKET

    CartRecurringEventTicket(Long eventId) {
        id = this.eventId =eventId
        RecurringEvents event = RecurringEvents.get(id)
        parentEventId = event.parentEventId
        name = "Ticket For " + event.parentEvent.name + ' - ' + event.start
        image = event.parentEvent.images ? event.parentEvent.images[0].name : null
        refresh()
    }

    CartRecurringEventTicket(Integer[] seats, Long sectionId, Long eventId) {
        type = NamedConstants.CART_OBJECT_TYPES.RECURRING_EVENT_VENUE_TICKET
        this.eventId = eventId;
        this.sectionId = sectionId;
        this.seats = seats;
        id = sectionId
        RecurringEvents _event = RecurringEvents.get(eventId)
        parentEventId = _event.parentEventId
        VenueLocationSection _section = VenueLocationSection.get(sectionId)
        name = "Ticket For " + _event.parentEvent.name + ' - ' + _event.start + " (" + _section.name + ")"
        image = _event.parentEvent.images ? _event.parentEvent.images[0].name : null
        refresh()
    }

    @Override
    void validate(Integer quantity) {
        RecurringEvents event = RecurringEvents.get(eventId)
        Integer available = event.parentEvent.maxTicket - event.totalSoldTicket;
        if(event.end.gmt(AppUtil.session.timezone) < new Date().gmt(AppUtil.session.timezone)) {
            throw new CartManagerException(this, "s:your.requested.event.date.expired", [event.parentEvent.name + ' - ' + event.start]);
        }
        if(quantity > available) {
            throw new CartManagerException(this, "s:requested.quantity.ticket.not.available", [quantity])
        }
        if (event.parentEvent.maxTicketPerCustomer && quantity > event.parentEvent.maxTicketPerCustomer) {
            throw new CartManagerException(this,  "s:you.can.buy.maximum.quantity.for.event", [event.parentEvent.maxTicketPerCustomer])
        }
    }

    @Override
    Integer available(Integer quantity) {
        RecurringEvents event = RecurringEvents.get(eventId)
        Integer available = event.parentEvent.maxTicket - event.totalSoldTicket;
        if(event.parentEvent.maxTicketPerCustomer && available > event.parentEvent.maxTicketPerCustomer) {
            available = event.parentEvent.maxTicketPerCustomer
        }
        return available
    }

    @Override
    String getLink() {
        return 'generalEvent/' + eventId + '?isRecurring=true';
    }

    @Override
    String getImageLink(String imageSize) {
        RecurringEvents event = RecurringEvents.get(eventId)
        int size = 100;
        if(imageSize.toInteger() > size) {
            size = 300
        }
        return 'resources/general-event/' + (event.parentEvent.images ? 'event-' + event.parentEvent.id + "/images/$size-" + event.parentEvent.images[0].name : "default/$size-default.png");
    }

    @Override
    void refresh() {
        effectivePrice = sectionId ? VenueLocationSection.get(sectionId).ticketPrice : RecurringEvents.get(eventId).parentEvent.ticketPrice;
    }

    @Override
    TaxProfile resolveTaxProfile() {
        RecurringEvents event = RecurringEvents.get(eventId)
        TaxProfile profile = event.parentEvent.taxProfile
        if(!profile) {
            profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile").toLong(0));
        }
        return profile;
    }
}

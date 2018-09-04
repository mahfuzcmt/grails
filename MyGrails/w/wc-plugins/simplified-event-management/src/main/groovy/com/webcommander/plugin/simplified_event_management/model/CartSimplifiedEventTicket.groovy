package com.webcommander.plugin.simplified_event_management.model

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.QuantityAdjustableCartObject
import com.webcommander.models.TaxableCartObject
import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import com.webcommander.throwables.CartManagerException
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxProfile

class CartSimplifiedEventTicket implements TaxableCartObject, QuantityAdjustableCartObject {

    String type = NamedConstants.CART_OBJECT_TYPES.SIMPLIFIED_EVENT_TICKET

    CartSimplifiedEventTicket(Long eventId) {
        id = eventId
        SimplifiedEvent event = SimplifiedEvent.get(eventId)
        name = "Ticket For " + event.name
        image = event.images ? event.images[0].name : null
        refresh()
    }

    @Override
    void validate(Integer quantity) {
        SimplifiedEvent event = SimplifiedEvent.get(id)
        Integer available = event.maxTicket - event.totalSoldTicket;
        if(event.endTime.gmt(AppUtil.session.timezone) < new Date().gmt(AppUtil.session.timezone)) {
            throw new CartManagerException(this, "your.requested.event.date.expired", [event.name]);
        }
        if(quantity > available) {
            throw new CartManagerException(this, "requested.quantity.ticket.not.available", [quantity])
        }
        if (event.maxTicketPerPerson && quantity > event.maxTicketPerPerson) {
            throw new CartManagerException(this,  "you.can.buy.maximum.quantity.for.event", [event.maxTicketPerPerson])
        }
    }

    @Override
    Integer available(Integer quantity) {
        SimplifiedEvent event = SimplifiedEvent.get(id)
        Integer available = event.maxTicket - event.totalSoldTicket;
        if(event.maxTicketPerPerson && available > event.maxTicketPerPerson) {
            available = event.maxTicketPerPerson
        }
        return available
    }

    @Override
    void refresh() {
        effectivePrice = SimplifiedEvent.get(id).ticketPrice;
    }

    @Override
    String getLink() {
        return 'simplifiedEvent/' + id;
    }

    @Override
    String getImageLink(String imageSize) {
        SimplifiedEvent event = SimplifiedEvent.get(id)
        int size = 100;
        if(imageSize.toInteger() > size) {
            size = 300
        }
        return 'resources/simplified-event/' + (event.images ? 'event-' + event.id + "/images/$size-" + event.images[0].name : "default/$size-default.png");
    }

    @Override
    TaxProfile resolveTaxProfile() {
        SimplifiedEvent event = SimplifiedEvent.get(id)
        TaxProfile profile = event.taxProfile
        if(!profile) {
            profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile").toLong(0));
        }
        return profile;
    }

}

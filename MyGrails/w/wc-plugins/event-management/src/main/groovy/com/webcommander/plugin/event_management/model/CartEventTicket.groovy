package com.webcommander.plugin.event_management.model

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.models.TaxableCartObject
import com.webcommander.plugin.event_management.Event
import com.webcommander.plugin.event_management.VenueLocationSection
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxProfile

class CartEventTicket implements TaxableCartObject {

    Long event;
    Long session;
    Long section;
    Integer[] seats
    String type = NamedConstants.CART_OBJECT_TYPES.EVENT_TICKET

    CartEventTicket(Integer[] seats, Long section, Long event, Long session) {
        this.event = event;
        this.section = section;
        this.session = session;
        this.seats = seats;
        id = section
        Event _event = Event.get(event)
        VenueLocationSection _section = VenueLocationSection.get(section)
        name = "Ticket For " + _event.name + " (" + _section.name + ")"
        image = _event.images ? _event.images[0].name : null
        refresh()
    }

    @Override
    void validate(Integer quantity) {
    }

    @Override
    Integer available(Integer quantity) {
        return quantity
    }

    @Override
    void refresh() {
        effectivePrice = VenueLocationSection.get(section).ticketPrice;
    }

    @Override
    String getLink() {
        return 'event/' + event + (session ? '/session/' + session : "");
    }

    @Override
    String getImageLink(String imageSize) {
        Event _event = Event.get(event)
        int size = 100;
        if(imageSize.toInteger() > size) {
            size = 300
        }
        return 'resources/event/' + (_event.images ? 'event-' + _event.id + "/images/$size-" + _event.images[0].name : "default/$size-default.png");
    }

    @Override
    TaxProfile resolveTaxProfile() {
        TaxProfile profile = Event.get(event).taxProfile;
        if(!profile) {
            profile = TaxProfile.get(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "default_tax_profile").toLong(0));
        }
        return profile;
    }


}

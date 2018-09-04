package com.webcommander.plugin.general_event.manager

import com.webcommander.events.AppEventManager
import com.webcommander.models.CartItem
import com.webcommander.plugin.general_event.model.CartGeneralEventTicket
import com.webcommander.plugin.general_event.model.CartRecurringEventTicket

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by arman on 1/18/2016.
 */
class VenueTicketCartManager {
    static ConcurrentHashMap<String, SortedSet<Integer>> seatsInCart = new ConcurrentHashMap<String, SortedSet<Integer>>();

    static {
        AppEventManager.on("cart-cleared cart-removed") { cart ->
            cart.cartItemList.findAll {it.object instanceof CartGeneralEventTicket || it.object instanceof CartRecurringEventTicket}.object.each {
                SortedSet<Integer> seatNumbers = seatsInCart["$it.sectionId#$it.eventId"]
                seatNumbers.removeAll(it.seats)
            }
        }
    }

    public static addedInCart(CartItem item) {
        String hash = "$item.object.sectionId#${item.object.eventId}"
        SortedSet<Integer> seatNumbers = seatsInCart[hash]
        if(!seatNumbers) {
            seatNumbers = new Collections.SynchronizedSortedSet<Integer>(new TreeSet<Integer>())
            seatsInCart[hash] = seatNumbers
        }
        seatNumbers.addAll(item.object.seats)
    }
}

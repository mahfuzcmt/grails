package com.webcommander.plugin.event_management.manager

import com.webcommander.events.AppEventManager
import com.webcommander.models.CartItem
import com.webcommander.plugin.event_management.model.CartEventTicket

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by zobair on 05/03/14.*/
class CartTicketManager {
    static ConcurrentHashMap<String, SortedSet<Integer>> seatsInCart = new ConcurrentHashMap<String, SortedSet<Integer>>();

    static {
        AppEventManager.on("cart-cleared") { cart ->
            cart.cartItemList.findAll {it.object instanceof CartEventTicket}.object.each {
                SortedSet<Integer> seatNumbers = seatsInCart["$it.section#$it.session#$it.event"]
                seatNumbers.removeAll(it.seats)
            }
        }
    }

    public static addedInCart(CartItem item) {
        String hash = "$item.object.section#${item.object.session}#${item.object.event}"
        SortedSet<Integer> seatNumbers = seatsInCart[hash]
        if(!seatNumbers) {
            seatNumbers = new Collections.SynchronizedSortedSet<Integer>(new TreeSet<Integer>())
            seatsInCart[hash] = seatNumbers
        }
        seatNumbers.addAll(item.object.seats)
    }
}

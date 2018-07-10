package com.webcommander.plugin.simplified_event_management.constants

import com.webcommander.constants.DomainConstants

/**
 * Created by arman on 11/17/2015.
 */
class SimplifiedEventConstants {
    static UPDATE_TICKET_STOCK = [
            (DomainConstants.UPDATE_STOCK.AFTER_ORDER): 'after.order',
            (DomainConstants.UPDATE_STOCK.AFTER_PAYMENT): 'after.payment'
    ];
    static EVENT_CHECKOUT_FIELD_TYPE = [
            LONG_TEXT: "long.text",
            TEXT: "text",
            SINGLE_SELECT_RADIO: "single.select.radio",
            SINGLE_SELECT_DROPDOWN: "single.select.dropdown",
            MULTISELECT_CHECKBOX: "multi.select.checkbox",
            MULTISELECT_LISTBOX: "multi.select.list"
    ]
}

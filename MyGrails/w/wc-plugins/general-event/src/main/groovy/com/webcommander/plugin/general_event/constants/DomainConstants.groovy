package com.webcommander.plugin.general_event.constants

class DomainConstants {

    static UPDATE_TICKET_STOCK = [
        (com.webcommander.constants.DomainConstants.UPDATE_STOCK.AFTER_ORDER): 'after.order',
        (com.webcommander.constants.DomainConstants.UPDATE_STOCK.AFTER_PAYMENT): 'after.payment'
    ];

    static TICKET_AVAILABILITY = [
        EVERYONE: "everyone",
        CUSTOMER: "customer",
        SELECTED: "selected"
    ]

    static DAILY_RECURRENCE_TYPE = [
        EVERY: "every",
        EVERYDAY: "everyday"
    ]

    static MONTHLY_RECURRENCE_TYPE = [
        DAY: "day",
        SELECTED: "selected"
    ]

    static YEARLY_RECURRENCE_TYPE = [
        ON: "on",
        ON_THE: "on_the"
    ]

    static RECURRENCE_END_TYPE = [
        NO_END_DATE: "no_end_date",
        END_AFTER_REPETITION: "end_after_repetition",
        END_BY_DATE: "end_by_date"
    ]

    static RECURRENCE_PATTERN = [
        DAILY: "daily",
        WEEKLY: "weekly",
        MONTHLY: "monthly",
        YEARLY: "yearly"
    ]

    static WEEK_DAYS = [
        SATURDAY: "saturday",
        SUNDAY: "sunday",
        MONDAY: "monday",
        TUESDAY: "tuesday",
        WEDNESDAY: "wednesday",
        THURSDAY: "thursday",
        FRIDAY: "friday"
    ]

    static EVENT_CHECKOUT_FIELD_TYPE = [
        LONG_TEXT: "long.text",
        TEXT: "text",
        SINGLE_SELECT_RADIO: "single.select.radio",
        SINGLE_SELECT_DROPDOWN: "single.select.dropdown",
        MULTISELECT_CHECKBOX: "multi.select.checkbox",
        MULTISELECT_LISTBOX: "multi.select.list"
    ]
}

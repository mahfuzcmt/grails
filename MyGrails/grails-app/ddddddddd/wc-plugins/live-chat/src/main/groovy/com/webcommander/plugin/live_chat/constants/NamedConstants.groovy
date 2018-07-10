package com.webcommander.plugin.live_chat.constants

/**
 * Created by sajedur on 22/10/2014.
 */
class NamedConstants {
    static QUICK_FILTER = [
        (DomainConstants.QUICK_FILTER.TODAY): "today",
        (DomainConstants.QUICK_FILTER.YESTERDAY): "yesterday",
        (DomainConstants.QUICK_FILTER.LAST_7_DAYS): "last.7.days",
        (DomainConstants.QUICK_FILTER.LAST_30_DAYS): "last.30.days"
    ]

    static CHAT_RATING_TYPE = [
        (DomainConstants.CHAT_RATING_TYPE.UP): "thumbs.up",
        (DomainConstants.CHAT_RATING_TYPE.DOWN): "thumbs.down"
    ]
}

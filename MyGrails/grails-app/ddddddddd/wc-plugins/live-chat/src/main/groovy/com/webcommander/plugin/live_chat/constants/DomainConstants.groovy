package com.webcommander.plugin.live_chat.constants

class DomainConstants {

    static CHAT_RATING_TYPE = [
        UP: "U",
        DOWN: "D"
    ]

    static  CHAT_MESSAGE_SENDER_TYPE = [
        AGENT: "agent",
        CUSTOMER: "customer"
    ]

    static NOTIFICATION_TYPE = [
        LEAVE_CHAT: "leave_chat",
        TRANSFER_CHAT_ACCEPT: "transfer_chat_accept",
        TRANSFER_CHAT_REJECT: "transfer_chat_reject",
        RATE_CHAT_GOOD: "rate_chat_good",
        RATE_CHAT_BAD: "rate_chat_bad",
        RATIND_CANCEL: "rating_cancel",
        FILE_TRANSFER: "file_transfer"
    ]

    static QUICK_FILTER = [
            TODAY: "today",
            YESTERDAY: "yesterday",
            LAST_7_DAYS: "last_7_days",
            LAST_30_DAYS: "last_30_days"
    ]

    static PUSH_NOTIFICATION_TYPE = [
        NEW_CHAT: 1,
        NEW_MESSAGE: 2,
        TRNSFER_REQUEST: 3,
        NOTIFICATION: 4
    ]
}

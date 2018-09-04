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
        INVITE_CHAT_ACCEPT: "invite_chat_accept",
        INVITE_CHAT_REJECT: "invite_chat_reject",
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

    static CHAT_ACTION_TYPE = [
                INVITE_OPERATOR    : "invite_operator",
                TRANSFER: "transfer",
                EXPORT_TXT: "export_txt",
                EMAIL_HISTORY: "email_history",
                BLOCK: "block",
                TERMINATE: "terminate"
    ]


    static PUSH_NOTIFICATION_TYPE = [
        NEW_CHAT: 1,
        NEW_MESSAGE: 2,
        TRNSFER_REQUEST: 3,
        NOTIFICATION: 4,
        INVITE_REQUEST: 5
    ]

    static CHAT_ROUTING_CONFIG = [
            BROADCAST_CHAT_IF_EVERY_OPERATOR_IS_A_PARTICULAR_DEPARTMENT_IS_BUSY: "broadcast_chat_if_every_operator_is_a_particular_department_is_busy",
            CHAT_LIMIT: "chat_limit",
            CHAT_ROUTING: "chat_routing"
    ]

    static CHAT_ROUTING_OPTION = [
            BROADCAST_TO_EVERYONE: "broadcast_to_everyone",
            ASSIGN_BASED_ON_DEPARTMENTS: "assign_based_on_departments"
    ]

    static CHAT_CONCIERGE_CONFIG = [
            ONLINE_TITLE: "online_title",
            MESSAGE_STYLE: "message_style",
            ONLINE_BUTTON_TEXT: "online_button_text",
            OFFLINE_TITLE: "offline_title",
            POSITION: "position",
            OFFLINE_BUTTON_TEXT: "offline_button_text"
    ]

    static CHAT_BOX_POSITION_VALUE = [
            Right_Bottom: "right_bottom",
            LEFT_BOTTOM: "left_bottom"
    ]

    static CHAT_MESSAGE_STYLE_VALUE = [
            PLAIN_TEXT: "plain_text",
            BUBBLED: "bubbled"
    ]

    static OFFLINE_EMAIL_RECIPIENT = "offline_email_recipient"

    static CHAT_SOUND_NOTIFICATION = [
            NEW_INCOMING_CHAT_SOUND: "new_incoming_chat_sound",
            NEW_CHAT_MESSAGE_SOUND: "new_chat_message_sound",
            DISCONNECT_SOUND: "disconnect_sound",
            OPERATOR_JOINS_A_CHAT_SOUND: "operator_joins_a_chat_sound",
            CHAT_IS_TRANSFERRED_TO_ANOTHER_OPERATOR_SOUND: "chat_is_transferred_to_another_operator_sound"
    ]

    static CHAT_VISITOR_INFORMATION_CONFIG = [
            ASK_INFO_BEFORE_CHAT_START: "ask_info_before_chat_start",
            ASK_NAME: "ask_name",
            IS_NAME_ASKING_MANDATORY: "is_name_asking_mandatory",
            ASK_CONTACT: "ask_contact",
            IS_CONTACT_ASKING_MANDATORY: "is_contact_asking_mandatory",
            ASK_EMAIL: "ask_email",
            IS_EMAIL_ASKING_MANDATORY: "is_email_asking_mandatory",
            ASK_DEPARTMENT: "ask_department",
            IS_DEPARTMENT_ASKING_MANDATORY: "is_department_asking_mandatory",
            ASK_MESSAGE: "ask_message",
            IS_MESSAGE_ASKING_MANDATORY: "is_message_asking_mandatory",
            ALLOW_SOCIAL_MEDIA_LOGIN: "allow_social_media_login",
            FACEBOOK_LOGIN: "facebook_login",
            GOOGLE_PLUS_LOGIN: "google_plus_login"
    ]

    static CHAT_MESSAGE_SETUP_CONFIG = [
            ONLINE_MESSAGE: "online_message",
            OFFLINE_MESSAGE: "offline_message",
            IS_ENABLED_DEFAULT_DEPARTMENT_MESSAGE: "is_enablde_default_department_message",
            IS_ENABLED_DEFAULT_MESSAGE_WHEN_NO_DEPARTMENT_SELECTED: "is_enabled_default_message_when_no_department_selected",
            DEFAULT_MESSAGE_TEXT_WHEN_NO_DEPARTMENT_SELECTED: "default_message_text_when_no_department_selected",
            IDLE_TIME_OUT_MESSAGE_SETTING : "idle_time_out_message_setting",
            INACTIVITY_TIME_PERIOD : "inactivity_time_period",
            NUMBER_OF_TIMES_MESSAGE_TO_BE_SENT : "number_of_times_message_to_be_sent",
            INACTIVITY_MESSAGE : "inactivity_message",
            IS_ENABLED_BUSY_MESSAGE_FOR_CUSTOMER: "is_enabled_busy_message_for_customer",
            BUSY_MESSAGE_TEXT_FOR_CUSTOMER: "busy_message_text_for_customer",
    ]


}

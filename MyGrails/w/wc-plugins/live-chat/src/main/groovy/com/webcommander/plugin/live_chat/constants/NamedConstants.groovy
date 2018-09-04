package com.webcommander.plugin.live_chat.constants


/**
 * Created by sajedur on 22/10/2014.
 */
class NamedConstants {

    static QUICK_FILTER = [
            (DomainConstants.QUICK_FILTER.TODAY)       : "today",
            (DomainConstants.QUICK_FILTER.YESTERDAY)   : "yesterday",
            (DomainConstants.QUICK_FILTER.LAST_7_DAYS) : "last.7.days",
            (DomainConstants.QUICK_FILTER.LAST_30_DAYS): "last.30.days"
    ]

    static CHAT_ACTION_TYPE = [
            (DomainConstants.CHAT_ACTION_TYPE.INVITE_OPERATOR)    : "invite.operator",
            (DomainConstants.CHAT_ACTION_TYPE.TRANSFER): "transfer",
            (DomainConstants.CHAT_ACTION_TYPE.EXPORT_TXT): "export.txt",
            (DomainConstants.CHAT_ACTION_TYPE.EMAIL_HISTORY): "email.history",
            (DomainConstants.CHAT_ACTION_TYPE.BLOCK): "block",
            (DomainConstants.CHAT_ACTION_TYPE.TERMINATE): "terminate"
    ]

    static CHAT_RATING_TYPE = [
            (DomainConstants.CHAT_RATING_TYPE.UP)  : "thumbs.up",
            (DomainConstants.CHAT_RATING_TYPE.DOWN): "thumbs.down"
    ]

    static CHAT_ROUTING_OPTION = [
            (DomainConstants.CHAT_ROUTING_OPTION.BROADCAST_TO_EVERYONE)      : "broadcast.to.everyone",
            (DomainConstants.CHAT_ROUTING_OPTION.ASSIGN_BASED_ON_DEPARTMENTS): "assign.based.on.departments"
    ]

    static CHAT_MESSAGE_STYLE = [
            (DomainConstants.CHAT_MESSAGE_STYLE_VALUE.PLAIN_TEXT): "plain.text",
            (DomainConstants.CHAT_MESSAGE_STYLE_VALUE.BUBBLED)   : "bubbled"
    ]

    static CHAT_MESSAGE_BOX_POSITION = [
            (DomainConstants.CHAT_BOX_POSITION_VALUE.Right_Bottom): "right.bottom",
            (DomainConstants.CHAT_BOX_POSITION_VALUE.LEFT_BOTTOM) : "left.bottom"
    ]
}
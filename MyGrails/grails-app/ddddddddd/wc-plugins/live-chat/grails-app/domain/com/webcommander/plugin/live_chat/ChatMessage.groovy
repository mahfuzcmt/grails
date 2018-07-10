package com.webcommander.plugin.live_chat

class ChatMessage {
    String message
    String senderType
    Boolean isNotification = false
    String notificationType
    Date created

    Collection<String> notificationArgs = []

    static belongsTo = [chat: Chat]
    static hasMany = [notificationArgs: String];

    static constraints = {
        notificationType(nullable: true);
        message(nullable: true, maxSize: 550);
    }
}

package com.webcommander.plugin.live_chat

class ChatMessage {
    String message
    String senderType
    Boolean isNotification = false
    String notificationType
    String name
    ChatAgent chatAgent
    Date created

    Collection<String> notificationArgs = []

    static belongsTo = [chat: Chat]
    static hasMany = [notificationArgs: String];

    static constraints = {
        notificationType(nullable: true);
        message(nullable: true, maxSize: 550);
        chatAgent(nullable: true);
        name(nullable: true);
    }
}

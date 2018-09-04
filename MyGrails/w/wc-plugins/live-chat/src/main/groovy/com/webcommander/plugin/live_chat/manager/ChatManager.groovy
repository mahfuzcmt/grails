package com.webcommander.plugin.live_chat.manager

import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import groovy.json.JsonOutput

import java.util.concurrent.ConcurrentHashMap

class ChatManager {
    private static ConcurrentHashMap<String, ChatData> CHAT_HOLDER = new ConcurrentHashMap();

    static pushChat(String key, ChatData data) {
        CHAT_HOLDER.put(key, data);
    }

    static Boolean activateOrphanChat(String chatId) {
        ChatData chatData = ChatManager.getChatData(chatId);
        if(chatData && !chatData.agentId) {
            AgentDataManger.removeChatFromOrphans(chatData);
            AgentDataManger.addChatToAgentAttendedList(AppUtil.loggedOperator, chatData)
            return true
        }
        return false
    }

    static Boolean addMessageToChat(Map messageData, ChatData chatData) {
        if(messageData ? messageData.message.length() < 500 : false) {
            return chatData.messages.add(messageData);
        }
        throw ApplicationRuntimeException("max.length.exceed");
    }

    static Boolean addMessageToChat(Map messageData, String chatId) {
        ChatData chatData = CHAT_HOLDER[chatId];
        if (chatData) {
            if (messageData.senderType == DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER) {
                Thread.start {
                    PushNotificationManager.notifyForNewChatMessage(chatData)
                }
            }
            return addMessageToChat(messageData, chatData)
        }
        return false
    }

    static Boolean sendChatMessage(String chatId, String message, String senderType) {
        boolean flag = false
        ChatData chatData = ChatManager.getChatData(chatId)
        if(senderType.equals(DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT) && chatData)   flag = true
        Map messageData = [
                message: message,
                senderType: senderType,
                time: new Date().time,
                isNotification: false,
                name : "System"
        ]
        if((chatData.agentId == AppUtil.loggedOperator && flag) || senderType.equals(DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)) {
             def result = ChatManager.addMessageToChat(messageData, chatData)
            if(result){
                Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
                def chatConfig = JsonOutput.toJson(liveChatConfig)
                chatData.sessions.each{
                    it.getBasicRemote().sendText(chatConfig)
                }
                return result;
            }

        }
        return false
    }


    static Boolean sendWelcomeMessage(String chatId, String message, String department) {
        ChatData chatData = ChatManager.getChatData(chatId)
        Map messageData = [
                message: message,
                senderType: DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT,
                time: new Date().time,
                isNotification: false,
                name : department
        ]
        return ChatManager.addMessageToChat(messageData, chatData);
        Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
        def chatConfig = JsonOutput.toJson(liveChatConfig)
        chatData.sessions.each{
            it.getBasicRemote().sendText(chatConfig)
        }
    }

    static void sendChatMessageFromSocket(String agentName, String chatId, String message, String senderType) {
        ChatData chatData = ChatManager.getChatData(chatId)
        Map messageData = [
                message: message,
                senderType: senderType,
                time: new Date().time,
                isNotification: false,
                name :  senderType.equals(DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)? chatData.name: agentName
        ]
        ChatManager.addMessageToChat(messageData, chatData);
    }

    static Boolean addNotificationToChat(ChatData chatData, String senderType, String notificationType, List<String> notificationArgs = [], Map extraFields = [:]) {
        Map notification = [
                senderType: senderType,
                message: "",
                notificationType: notificationType,
                notificationArgs: notificationArgs,
                time: new Date().getTime(),
                isNotification: true
        ];
        notification << extraFields
        if(senderType == DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER) {
            Thread.start {
                PushNotificationManager.notifyForNotification(chatData, notificationType)
            }
        }
        Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
        def chatConfig = JsonOutput.toJson(liveChatConfig)
        chatData.sessions.each{
            it.getBasicRemote().sendText(chatConfig)
        }
        return chatData.messages.add(notification)
    }

    static Boolean addNotificationToChat(String chatId, String senderType, String notificationType, List<String> notificationArgs = [], Map extraFields = [:]) {
        ChatData chatData = CHAT_HOLDER[chatId];
        return addNotificationToChat(chatData, senderType, notificationType, notificationArgs, extraFields)
    }

    static ChatData getChatData(String chatId) {
        return chatId ? CHAT_HOLDER[chatId] : null;
    }

    static ChatData getChatData() {
        return getChatData(AppUtil.session.live_chat_ref)
    }

    static Boolean removeChat(chatId) {
        return CHAT_HOLDER.remove(chatId)
    }

    static Integer getNewMessageCount(ChatData chatData) {
        Integer newCount = 0;
        Integer total = chatData.messages.size();
        for (int i = total - 1; i >= 0; i--) {
            Map message = chatData.messages[i];
            if (message.time >= chatData.lastRead) {
                newCount++
            }
        }
        return newCount;
    }

    static Long getIdleTime(ChatData chatData) {
        Long idleTimeInMilliSec = -1;
        Integer total = chatData.messages.size();
        for (int i = total - 1; i >= 0; i--) {
            Map message = chatData.messages[i];
            if(message.senderType == DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT){
                return  (new Date().time - message.time);
            }
        }
        return idleTimeInMilliSec;
    }

    static List<Map> getUpdatedMessagesByChat(ChatData chatData, Long lastUpdate, Long tempLastUpdate) {
        List messages = [];
        if(chatData && lastUpdate) {
            for (int i = chatData.messages.size() - 1; i >= 0; i--) {
                Map message = chatData.messages[i]
                if(message.time >= lastUpdate && message.time < tempLastUpdate) {
                    messages.add(message);
                } else {
                    break;
                }
            }
            messages = messages.reverse();
        } else {
            messages = chatData.messages;
        }
        return messages;
    }

}

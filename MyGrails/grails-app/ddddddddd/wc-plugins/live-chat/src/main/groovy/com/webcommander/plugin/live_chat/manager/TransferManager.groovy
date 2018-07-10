package com.webcommander.plugin.live_chat.manager

import com.webcommander.admin.Operator
import com.webcommander.admin.Operator

import java.util.concurrent.ConcurrentHashMap

class TransferManager {
    private static ConcurrentHashMap<String, Map> TRANSFER_REQUEST = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, List<Map>> TRANSFER_NOTIFICATION = new ConcurrentHashMap();

    static void sendRequest(String chatId, Long agentId, Long sender, String message) {
        TRANSFER_REQUEST[chatId]  = [
                agentId: agentId,
                message: message,
                sender: sender,
                senderName: Operator.get(sender).fullName.encodeAsBMHTML()
        ]
    }

    static Boolean isRequested(String chatId, Long agentId) {
        if(TRANSFER_REQUEST[chatId] && TRANSFER_REQUEST[chatId].agentId == agentId) {
            return true
        }
        return false
    }
    
    static Map getTransferRequest(String chatId) {
        return TRANSFER_REQUEST[chatId];
    }

    static void removeRequest(String chatId) {
        TRANSFER_REQUEST.remove(chatId);
    }

    static void addNotification(String agentId, Map notification) {
        notification.time = new Date().gmt().getTime();
        if(!TRANSFER_NOTIFICATION[agentId]) {
            TRANSFER_NOTIFICATION[agentId] = [];
        }
        TRANSFER_NOTIFICATION[agentId].add(notification)
    }

    static List getNotifications(String agentId, Long lastUpdate) {
        List agentNotifications = TRANSFER_NOTIFICATION[agentId] ?: [];
        agentNotifications.removeAll {
            it.time < lastUpdate
        }
        return agentNotifications;
    }
}

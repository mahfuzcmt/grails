package com.webcommander.plugin.live_chat.manager

import com.webcommander.plugin.live_chat.models.AgentData
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.plugin.live_chat.models.WcDevice
import com.webcommander.tenant.Thread

import java.util.concurrent.ConcurrentHashMap

class AgentDataManger {

    private static ConcurrentHashMap<Long, AgentData> AGENT_DATA_HOLDER = new ConcurrentHashMap();
    private static List<Map> ORPHAN_CHATS = Collections.synchronizedList(new ArrayList<ChatData>());

    static AgentData getAgentData(Long agentId) {
        AgentData agentData
        if(!AGENT_DATA_HOLDER[agentId]) {
            agentData = AGENT_DATA_HOLDER[agentId] = new AgentData();
        } else {
            agentData = AGENT_DATA_HOLDER[agentId];
        }
        return  agentData;
    }

    static List<ChatData> getAgentAttendedList(Long agentId) {
        AgentData agentData = getAgentData(agentId);
        return agentData.attendedChats;
    }

    static Boolean addChatToAgentAttendedList(Long agentId, ChatData chatData) {
        AgentData agentData = getAgentData(agentId);
        chatData.agentId = agentId;
        return agentData.addToAttendedChats(chatData);
    }

    static Boolean removeChatFromAgentAttendedList(Long agentId, ChatData chatData) {
        AgentData agentData = AGENT_DATA_HOLDER[agentId];
        return agentData.removeFromAttendedChats(chatData)
    }

    static sendTransferRequest(Long requestedId, Long chatId, Long requesterId, String message) {
        AgentData agentData = getAgentData(requestedId);
        agentData.addToTransferRequests(chatId, requesterId, message)
        Thread.start {
            ChatData chatData = ChatManager.getChatData(chatId + "");
            PushNotificationManager.notifyForTransferRequest(chatData, requestedId)
        }
    }

    static Boolean removeChatFromAgentTransferRequestList(Long agentId, Long chatId) {
        AgentData agentData = AGENT_DATA_HOLDER[agentId];
        return agentData.removeFromTransferRequests(chatId);
    }

    static Boolean addChatToOrphans(ChatData chatData) {
        Boolean returnVal = ORPHAN_CHATS.add(chatData)
        Thread.start {
            PushNotificationManager.notifyForOrphanChat(chatData)
        }
        return returnVal
    }

    static  Boolean removeChatFromOrphans(ChatData chatData) {
        return ORPHAN_CHATS.remove(chatData)
    }

    static Boolean transferChat(Long toAgent, Long fromAgent, ChatData chatData) {
        removeChatFromAgentAttendedList(fromAgent, chatData);
        addChatToAgentAttendedList(toAgent, chatData);
    }

    static Map findUpdateInfoByAgent(Long agentId) {
        List<Map> attendedChats = [];
        List<Map> orphanChats = [];
        AgentData agentData  = getAgentData(agentId);
        agentData.attendedChats.each { chat ->
            Map agentChat = [
                chatId: chat.id,
                name: chat.name,
                newMessage: ChatManager.getNewMessageCount(chat)
            ];
            attendedChats.add(agentChat);
        }
        ORPHAN_CHATS.each { chat ->
            Map orphanChat = [
                chatId: chat.id,
                name: chat.name
            ]
            orphanChats.add(orphanChat)
        }
        return [attendedChats: attendedChats, orphanChats: orphanChats, transferRequests: agentData.transferRequests]
    }

    static List<WcDevice> getInactiveDevices() {
        List<WcDevice> devices = [];
        AGENT_DATA_HOLDER.each {
            devices.addAll(it.value.getInactiveDevices());
        }
        return devices
    }

    static List<WcDevice> getAllDevice() {
        List<WcDevice> devices = [];
        AGENT_DATA_HOLDER.each {
            devices.addAll(it.value.getDevices());
        }
        return devices
    }

    static List<WcDevice> getInactiveDevicesByAgent(Long agentId) {
        AgentData agentData = getAgentData(agentId)
        return agentData.inactiveDevices
    }

    static List<WcDevice> getDevicesByAgent(Long agentId) {
        AgentData agentData = getAgentData(agentId)
        return agentData.deviceList
    }

    static Integer deviceCountByAgent(Long agentId) {
        return getDevicesByAgent(agentId).size();
    }
}

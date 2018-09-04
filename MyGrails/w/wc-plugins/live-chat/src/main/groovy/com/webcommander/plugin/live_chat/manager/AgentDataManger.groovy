package com.webcommander.plugin.live_chat.manager

import com.webcommander.constants.DomainConstants as DC
import com.webcommander.plugin.live_chat.Chat
import com.webcommander.plugin.live_chat.ChatDepartment
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.ChatOperatorProfile
import com.webcommander.plugin.live_chat.models.AgentData
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.plugin.live_chat.models.WcDevice
import com.webcommander.tenant.Thread
import com.webcommander.util.AppUtil

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

    static sendInviteRequest(Long requestedId, Long chatId, Long requesterId, String message) {
        AgentData agentData = getAgentData(requestedId);
        agentData.addToInviteRequests(chatId, requesterId, message)
        Thread.start {
            ChatData chatData = ChatManager.getChatData(chatId + "");
            PushNotificationManager.notifyForInviteRequest(chatData, requestedId)
        }
    }

    static Boolean removeChatFromAgentTransferRequestList(Long agentId, Long chatId) {
        AgentData agentData = AGENT_DATA_HOLDER[agentId];
        return agentData.removeFromTransferRequests(chatId);
    }

    static Boolean removeChatFromAgentInviteRequestList(Long agentId, Long chatId) {
        AgentData agentData = AGENT_DATA_HOLDER[agentId];
        return agentData.removeFromInviteRequests(chatId);
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

    static Boolean inviteChat(Long toAgent, ChatData chatData) {
        AgentData agentData = getAgentData(toAgent);
        return agentData.addToAttendedChats(chatData);
    }

    static boolean isEveryOperatorOfaParticularDepartmentBusy(ChatDepartment chatDepartment, config) {
        def isBusy = true
        chatDepartment.operators.each {
            AgentData agentData  = getAgentData(it.id);
            def operatorProfile = ChatOperatorProfile.findByOperatorId(it.id)
            def chatLimit = operatorProfile? operatorProfile.chatLimit : config[DomainConstants.CHAT_ROUTING_CONFIG.CHAT_LIMIT]
            if(agentData.attendedChats.size() < chatLimit){
                isBusy = false
            }
        }
        return isBusy
    }

    static Map findUpdateInfoByAgent(Long agentId) {
        List<Map> attendedChats = [];
        List<Map> orphanChats = [];
        AgentData agentData  = getAgentData(agentId);
        Map config = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT);
        agentData.attendedChats.each { chat ->
            Long idleTimeInMilliSec =  ChatManager.getIdleTime(chat)
            Long inactivityTimePeriodConfig = ((config[DomainConstants.CHAT_MESSAGE_SETUP_CONFIG.INACTIVITY_TIME_PERIOD].toLong(0) * 60 * 1000));
            Long numberOfTimesMessageToBeSent = config[DomainConstants.CHAT_MESSAGE_SETUP_CONFIG.NUMBER_OF_TIMES_MESSAGE_TO_BE_SENT].toLong(0);
            if((idleTimeInMilliSec >= inactivityTimePeriodConfig) && (numberOfTimesMessageToBeSent > chat.numbersOfIdleAlertSent) && (numberOfTimesMessageToBeSent != 0)) {
                ChatManager.sendChatMessage(chat.id + "", config[DomainConstants.CHAT_MESSAGE_SETUP_CONFIG.INACTIVITY_MESSAGE], DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT)
                chat.numbersOfIdleAlertSent = chat.numbersOfIdleAlertSent + 1;
                ChatManager.pushChat(chat.id + "", chat);
            }
            Map agentChat = [
                    chatId: chat.id,
                    name: chat.name,
                    newMessage: ChatManager.getNewMessageCount(chat)
            ];
            attendedChats.add(agentChat);
        }
        def operatorProfile = ChatOperatorProfile.findByOperatorId(agentId)
        def chatLimit = operatorProfile? operatorProfile.chatLimit : config[DomainConstants.CHAT_ROUTING_CONFIG.CHAT_LIMIT]
        def skills = ChatDepartment.createCriteria().list{operators{ eq("id", agentId)}}
        if(agentData.attendedChats.size() < chatLimit){
            ORPHAN_CHATS.each { chat ->
                Map orphanChat = [
                        chatId: chat.id,
                        name: chat.name
                ]
                Chat chatObj = Chat.get(chat.id)

                if(!chatObj?.chatDepartment || (config[DomainConstants.CHAT_ROUTING_CONFIG.CHAT_ROUTING] == DomainConstants.CHAT_ROUTING_OPTION.BROADCAST_TO_EVERYONE)){
                    orphanChats.add(orphanChat)
                } else{
                    if(chatObj.chatDepartment){
                        def isSkillFound= skills.find {it.id == chatObj.chatDepartment.id}
                        if(isSkillFound){
                            orphanChats.add(orphanChat)
                        } else if(config[DomainConstants.CHAT_ROUTING_CONFIG.BROADCAST_CHAT_IF_EVERY_OPERATOR_IS_A_PARTICULAR_DEPARTMENT_IS_BUSY] == "true"){
                            if(isEveryOperatorOfaParticularDepartmentBusy(chatObj.chatDepartment, config)){
                                orphanChats.add(orphanChat)
                            }
                        }
                    }
                }
            }
        }
        return [attendedChats: attendedChats, orphanChats: orphanChats, transferRequests: agentData.transferRequests, inviteRequests: agentData.inviteRequests]
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

    static boolean isChatDataAvailabeWithAnyAgent(ChatData chatData) {
        def isAvailable = AGENT_DATA_HOLDER.find {agentData ->
            agentData.value.getAttendedChats().find {it.id == chatData.id}
        }
        return isAvailable
    }

}

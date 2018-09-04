package com.webcommander.plugin.live_chat.controllers.rest.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.live_chat.Chat
import com.webcommander.plugin.live_chat.ChatTag
import com.webcommander.plugin.live_chat.LiveChatService
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.AgentDataManger
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.AgentData
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import grails.converters.JSON
import javapns.devices.Device

class ApiLiveChatAdminController extends RestProcessor{
    LiveChatService liveChatService

    def updateInfo() {
        Long tempLastUpdate = new Date().getTime();
        Map model = AgentDataManger.findUpdateInfoByAgent(AppUtil.loggedOperator);
        model.lastUpdate = tempLastUpdate;
        rest model
    }

    def activateOrphanChat() {
        if(ChatManager.activateOrphanChat(params.chatId)) {
            rest status: "success";
        } else {
            rest status: "error", statusType: "taken", message: g.message(code: "chat.not.available") ;
        }
    }

    def updateActiveChat() {
        Long lastUpdate = params.long("lastUpdate");
        Long tempLastUpdate = new Date().getTime();
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData) {
            List<Map> messages = ChatManager.getUpdatedMessagesByChat(chatData, lastUpdate, tempLastUpdate);
            chatData.lastRead = tempLastUpdate;
            rest messages: messages, chatId: params.chatId, lastUpdate: tempLastUpdate, customerName: chatData.name
        } else if(liveChatService.isTerminated(params.long("chatId"))) {
            rest status: "error", isTerminated: true;
        } else {
            rest status: "error";
        }
    }

    @License(required = "allow_live_chat_feature")
    def sendChatMessage() {
        String errorMessage = "message.send.failed";
        Boolean success = false;
        try {
            ChatManager.sendChatMessage(params.chatId, params.message, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT)
        } catch (ApplicationRuntimeException ex) {
            success = false
            errorMessage = ex.message
        }
        if(success) {
            rest status: "success"
        } else {
            rest status: "error", message: errorMessage
        }
    }

    def tagList() {
        def results
        String chatId = params.chatId
        Map map = [:]
        if(chatId) {
            Chat chat = Chat.get(chatId)
            results = chat ? chat.tags : [];
            map.tags = results.collect {
                [id: it.id, name: it.name]
            }
        }
        map.allTags = ChatTag.list().collect {
            [id: it.id, name: it.name]
        }
        rest map
    }

    @License(required = "allow_live_chat_feature")
    def tagAdd() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData.agentId == AppUtil.loggedOperator && liveChatService.addTagToChat(params)) {
            rest([status: "success"]);
        } else {
            rest([status: "error"]);
        }
    }

    def removeTagFromChat() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData.agentId == AppUtil.loggedOperator && liveChatService.removeTagFromChat(params)) {
            rest([status: "success"]);
        } else {
            rest([status: "error"] );
        }
    }

    @License(required = "allow_live_chat_feature")
    def terminateChat() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData.agentId == AppUtil.loggedOperator) {
            liveChatService.leaveChatByAdmin(chatData, session.admin);
            rest([status: "success"]);
        } else {
            rest([status: "error"]);
        }
    }

    @License(required = "allow_live_chat_feature")
    def notificationStart() {
        AgentData agentData = AgentDataManger.getAgentData(AppUtil.loggedOperator);
        String deviceToken = params.token ?: session.device_token;
        Device device = agentData.setDeviceStatus(deviceToken, false);
        if(device) {
            rest([status: "success"] as JSON)
        } else {
            rest([status: "success"] as JSON)
        }
    }

    @License(required = "allow_live_chat_feature")
    def notificationStop() {
        AgentData agentData = AgentDataManger.getAgentData(AppUtil.loggedOperator);
        String deviceToken = params.token ?: session.device_token;
        Device device = agentData.setDeviceStatus(deviceToken, true);
        if(device) {
            rest([status: "success"] as JSON)
        } else {
            rest([status: "success"] as JSON)
        }
    }
}

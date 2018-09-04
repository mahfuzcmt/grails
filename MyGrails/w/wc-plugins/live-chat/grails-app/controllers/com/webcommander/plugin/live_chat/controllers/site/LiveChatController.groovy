package com.webcommander.plugin.live_chat.controllers.site

import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants as DC
import com.webcommander.listener.SessionManager
import com.webcommander.plugin.live_chat.Chat
import com.webcommander.plugin.live_chat.LiveChatService
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.AgentManager
import com.webcommander.plugin.live_chat.manager.ChatFileManager
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.util.AppUtil
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile
import com.webcommander.plugin.live_chat.constants.DomainConstants as LCD

class LiveChatController {
    LiveChatService liveChatService
    FileService fileService

    def initChat() {
        def config = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT)
        String askForInfo = config[LCD.CHAT_VISITOR_INFORMATION_CONFIG.ASK_INFO_BEFORE_CHAT_START]
        ChatData chatData = ChatManager.getChatData(session.live_chat_ref)
        if(askForInfo == "false" && !chatData && AgentManager.agentCount) {
            params.name = "Guest"
            chatData = liveChatService.initChat(params)
        }
        if(chatData) {
            forward(action: "enterChat", params: params)
            return
        } else {
            session.live_chat_ref = null
        }
        render(view: "/plugins/live_chat/site/liveChat/initChat", model: [agentCount: AgentManager.agentCount, config : config])
    }

    def chatPopup() {
        render(view: "/plugins/live_chat/site/liveChat/chatPopup")
    }

    def sendOfflineMessage() {
        try {
            liveChatService.sendOfflineMessage(params);
            String html = g.include(view: "/plugins/live_chat/site/liveChat/offlineEmailSuccessMessage.gsp").toString();
            render([status: "success", html: html] as JSON);
        } catch (Exception ex) {
            render([status: "error", message: g.message(code:  "message.send.failed")] as JSON);
        }
    }

    def enterChat() {
        if(!session.live_chat_ref) {
            liveChatService.initChat(params);
        }
        Chat chat = Chat.get(session.live_chat_ref);
        ChatData chatData = ChatManager.getChatData(session.live_chat_ref)
        InformationEncrypter encrypter = new InformationEncrypter()
        encrypter.hideInfo(chatData.id + "");
        String encryptedId = encrypter.toString();
        render(view: "/plugins/live_chat/site/liveChat/chatWindow", model: [chat: chat, chatData: chatData, encryptedId: encryptedId])
    }

    def sendChatMessage() {
        def result = ChatManager.sendChatMessage(session.live_chat_ref, params.message, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    def chatUpdate() {
        Long tempLastUpdate = new Date().getTime();
        Long lastUpdate = params.long("lastUpdate");
        ChatData chatData = ChatManager.getChatData(session.live_chat_ref);
        List messages = [];
        if(chatData) {
            messages = ChatManager.getUpdatedMessagesByChat(chatData, lastUpdate, tempLastUpdate)
        } else if(liveChatService.isTerminated(session.live_chat_ref.toLong())) {
            render([status: "error", isTerminated: true] as JSON)
            return;
        }
        render([messages: messages, lastUpdate: tempLastUpdate] as JSON)
    }

    def sendChatToMailPopup() {
        ChatData chatData = ChatManager.getChatData();
        render(view: "/plugins/live_chat/site/liveChat/sendChatToMailPopup", model: [chatData: chatData])
    }

    def sendChatToMail() {
        ChatData chatData = ChatManager.getChatData(session.live_chat_ref);
        if(chatData) {
            chatData.historyRecipient = params.recipient;
            render([status: "success", message: g.message(code: "history.send.request.success.message", args: [params.recipient])] as JSON)
        } else {
            InformationEncrypter encrypter = new InformationEncrypter(params.encryptedId, false);
            String chatId = encrypter.getHiddenInfos()[0];
            Chat chat = Chat.get(chatId);
            liveChatService.sendChatToMail(chat, params.recipient)
            render([status: "success", message: g.message(code: "transcript.send.success")] as JSON)
        }
    }

    def sendFilePopup() {
        render(view: "/plugins/live_chat/site/liveChat/sendFilePopup")
    }

    def sendFile() {
        String chatId = session.live_chat_ref
        ChatData chatData = ChatManager.getChatData(chatId)
        MultipartFile file = request.getFile('file')
        if (file.size <= (5 * 1024 * 1024)) {
            String tempPath = SessionManager.publicTempFolder.absolutePath
            String fileName = file.originalFilename
            fileService.uploadFile(file, null, fileName, null, tempPath)
            String fileIdentifier = ChatFileManager.pushFile(tempPath + File.separator + fileName, chatId)
            List<String> notificationArgs = [chatData.name, fileName]
            Map notificationExtraFields = [
                    fileName: file.originalFilename,
                    fileIdentifier: fileIdentifier
            ]
            ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER, DomainConstants.NOTIFICATION_TYPE.FILE_TRANSFER, notificationArgs, notificationExtraFields)
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "can.not.upload.over")] as JSON)
        }
    }

    def rateChat() {
        if(liveChatService.rateChat(params)) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    def chatLeave() {
        String chatId = session.live_chat_ref
        ChatData chatData = ChatManager.getChatData(chatId);
        if(chatData) {
            List<String> notificationArgs = [chatData.name];
            ChatManager.addNotificationToChat(chatData, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER, DomainConstants.NOTIFICATION_TYPE.LEAVE_CHAT, notificationArgs)
            liveChatService.leaveChat(chatData, chatData.agentId);
        }
        session.live_chat_ref = null
        render([status: "success"] as JSON)
    }

    def downloadFile() {
        String id = params.id;
        Map fileRef = ChatFileManager.getFile(id);
        if (fileRef && fileRef.chatId == session.live_chat_ref) {
            String filePath = fileRef.filePath;
            File file = new File(filePath);
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            InputStream inputStream = new FileInputStream(file)
            response.outputStream << inputStream
            inputStream.close()
        } else {
            render text:  g.message(code: "file.not.available")
        }
    }

    def isChatRunning() {
        ChatData chatData = ChatManager.getChatData();
        render([status: chatData ? "success" : "error"] as JSON);
    }
}

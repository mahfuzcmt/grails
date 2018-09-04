package com.webcommander.plugin.live_chat.controllers.admin

import com.webcommander.admin.Operator
import com.webcommander.authentication.annotations.License
import com.webcommander.common.FileService
import com.webcommander.listener.SessionManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.live_chat.Chat
import com.webcommander.plugin.live_chat.ChatAgent
import com.webcommander.plugin.live_chat.ChatDepartment
import com.webcommander.plugin.live_chat.ChatOperatorProfile
import com.webcommander.plugin.live_chat.ChatTag
import com.webcommander.plugin.live_chat.LiveChatService
import com.webcommander.constants.DomainConstants as DC
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.AgentDataManger
import com.webcommander.plugin.live_chat.manager.AgentManager
import com.webcommander.plugin.live_chat.manager.ChatFileManager
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

import javax.servlet.http.HttpSession

class LiveChatAdminController {
    LiveChatService liveChatService
    FileService fileService

    @License(required = "allow_live_chat_feature")
    def loadAppView() {
        render(view: "/plugins/live_chat/admin/liveChat/appView");
    }

    @License(required = "allow_live_chat_feature")
    def config() {
        Map config = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT);
        render(view: "/plugins/live_chat/admin/liveChat/config", model: [config: config]);
    }

    def updateInfo() {
        Long lastUpdate = params.long("lastUpdate");
        Long tempLastUpdate = new Date().getTime();
        Map model = AgentDataManger.findUpdateInfoByAgent(session.admin);
        model.lastUpdate = tempLastUpdate;
        render(model as JSON);
    }

    def isActiveChat() {
        Boolean isActive = liveChatService.isActivated(session.admin);
        render([status: "success", isActive: isActive] as JSON);
    }

    def activateOrphanChat() {
        if(ChatManager.activateOrphanChat(params.chatId)) {
            String agentName = liveChatService.getOperatorName(session.admin);
            List<String> args = [agentName.encodeAsBMHTML()];
            ChatManager.addNotificationToChat(params.chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.INVITE_CHAT_ACCEPT, args)
            render([status: "success"] as JSON);
        } else {
            render([status: "error", statusType: "taken", message: g.message(code: "chat.not.available")] as JSON);
        }
    }

    def updateActiveChat() {
        Long lastUpdate = params.long("lastUpdate");
        Long tempLastUpdate = new Date().getTime();
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData) {
            List<Map> messages = ChatManager.getUpdatedMessagesByChat(chatData, lastUpdate, tempLastUpdate);
            chatData.lastRead = tempLastUpdate;
            response.setContentType("application/json")
            render([messages: messages, chatId: params.chatId, lastUpdate: tempLastUpdate, customerName: chatData.name] as JSON)
        } else if(liveChatService.isTerminated(params.long("chatId"))) {
            render([status: "error", isTerminated: true] as JSON);
        } else {
            render([status: "error"] as JSON);
        }
    }

    @License(required = "allow_live_chat_feature")
    def sendChatMessage() {
        String errorMessage = "message.send.failed";
        Boolean success = false;
        try {
            success = ChatManager.sendChatMessage(params.chatId, params.message, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT)
        } catch (ApplicationRuntimeException ex) {
            success = false
            errorMessage = ex.message
        }
        if(success) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: errorMessage] as JSON)
        }
    }

    @License(required = "allow_live_chat_feature")
    def activateChat() {
        if(liveChatService.activateChat(session.admin)) {
            render([status: "success", message: g.message(code: "chat.activate.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "chat.activate.failed")] as JSON)
        }
    }

    def deactivateChat() {
        if(liveChatService.deactivateChat(session.admin)) {
            render([status: "success", message: g.message(code: "chat.deactivate.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "chat.deactivate.failed")] as JSON)
        }
    }

    /* Chat Tab */
    @License(required = "allow_live_chat_feature")
    def loadChat() {
        Boolean isAgent = liveChatService.isActivated(session.admin);
        Chat chat = params.chatId ? Chat.get(params.chatId) : null;
        def duration = 0;
        if(chat){
            duration = getMilliSeconds(new Date().gmt()) - getMilliSeconds(chat?.created)
        }
        render(view: "/plugins/live_chat/admin/liveChat/loadChat", model: [isAgent: isAgent, chat: chat, duration : duration]);
    }

    long getMilliSeconds(date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    @License(required = "allow_live_chat_feature")
    def addTagToChat() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData.agentId == session.admin && liveChatService.addTagToChat(params)) {
            render([status: "success"] as JSON);
        } else {
            render([status: "error"] as JSON);
        }
    }

    def removeTagFromChat() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData.agentId == session.admin && liveChatService.removeTagFromChat(params)) {
            render([status: "success"] as JSON);
        } else {
            render([status: "error"] as JSON);
        }
    }

    @License(required = "allow_live_chat_feature")
    def terminateChat() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
        if(chatData) {
            liveChatService.leaveChatByAdmin(chatData, session.admin);
            render([status: "success"] as JSON);
        } else {
            render([status: "error"] as JSON);
        }
    }

    @License(required = "allow_live_chat_feature")
    def initTransferRequest() {
        List<Long> agentIds = AgentManager.agentIds;
        def filter =  {
            inList ("id", agentIds)
            not {
                eq("id", AppUtil.loggedOperator)
            }
        }
        render(view: "/plugins/live_chat/admin/liveChat/initTransfer", model: [filter: filter])
    }

    @License(required = "allow_live_chat_feature")
    def initInviteRequest() {
        List<Long> agentIds = AgentManager.agentIds;
        def filter =  {
            inList ("id", agentIds)
            not {
                eq("id", AppUtil.loggedOperator)
            }
        }
        render(view: "/plugins/live_chat/admin/liveChat/initInvite", model: [filter: filter])
    }

    @License(required = "allow_live_chat_feature")
    def sendTransferRequest() {
        AgentDataManger.sendTransferRequest(params.long("agent"), params.long("chatId"), session.admin, params.message)
        render([status: "success", message: g.message(code: "transfer.request.send.success")] as JSON)
    }

    @License(required = "allow_live_chat_feature")
    def sendInviteRequest() {
        AgentDataManger.sendInviteRequest(params.long("agent"), params.long("chatId"), session.admin, params.message)
        render([status: "success", message: g.message(code: "invite.request.send.success")] as JSON)
    }

    def sendChatToMailPopup() {
        ChatData chatData = ChatManager.getChatData();
        render(view: "/plugins/live_chat/admin/liveChat/sendChatToMailPopup", model: [chatData: chatData])
    }

    def sendChatToMail() {
        ChatData chatData = ChatManager.getChatData(params.chatId);
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

    def acceptTransferRequest() {
        String chatId = params.chatId;
        ChatData chatData = ChatManager.getChatData(chatId);
        Long agentId = session.admin;
        Long requesterId = params.long("requesterId")
        String receiverName = liveChatService.getOperatorName(agentId);
        String requesterName = liveChatService.getOperatorName(requesterId);
        if (chatData) {
            AgentDataManger.transferChat(agentId, requesterId, chatData);
            AgentDataManger.removeChatFromAgentTransferRequestList(agentId, chatId.toLong());
            List<String> args = [receiverName.encodeAsBMHTML(), requesterName.encodeAsBMHTML()];
            ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.TRANSFER_CHAT_ACCEPT, args)
            render([status: "success"] as JSON)
        } else {
            AgentDataManger.removeChatFromAgentTransferRequestList(agentId, chatId.toLong());
            render([status: "error", errorType: "not_available", message: g.message(code: "chat.not.available")] as JSON)
        }
    }

    def acceptInviteRequest() {
        String chatId = params.chatId;
        ChatData chatData = ChatManager.getChatData(chatId);
        Long agentId = session.admin;
        Long requesterId = params.long("requesterId")
        String receiverName = liveChatService.getOperatorName(agentId);
        String requesterName = liveChatService.getOperatorName(requesterId);
        if (chatData) {
            AgentDataManger.inviteChat(agentId, chatData);
            AgentDataManger.removeChatFromAgentInviteRequestList(agentId, chatId.toLong());
            List<String> args = [receiverName.encodeAsBMHTML(), requesterName.encodeAsBMHTML()];
            ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.INVITE_CHAT_ACCEPT, args)
            render([status: "success"] as JSON)
        } else {
            AgentDataManger.removeChatFromAgentInviteRequestList(agentId, chatId.toLong());
            render([status: "error", errorType: "not_available", message: g.message(code: "chat.not.available")] as JSON)
        }
    }

    def rejectTransferRequest() {
        String chatId = params.chatId;
        ChatData chatData = ChatManager.getChatData(chatId);
        Long agentId = session.admin;
        Long requesterId = params.long("requesterId")
        String receiverName = liveChatService.getOperatorName(agentId);
        String requesterName = liveChatService.getOperatorName(requesterId);
        if (chatData) {
            AgentDataManger.removeChatFromAgentTransferRequestList(agentId, chatData.id);
            List<String> args = [receiverName.encodeAsBMHTML(), requesterName.encodeAsBMHTML()];
            ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.TRANSFER_CHAT_REJECT, args)
            render([status: "success"] as JSON)
        } else {
            AgentDataManger.removeChatFromAgentTransferRequestList(agentId, chatId.toLong());
            render([status: "error", errorType: "not_available", message: g.message(code: "chat.not.available")] as JSON)
        }
    }


    def rejectInviteRequest() {
        String chatId = params.chatId;
        ChatData chatData = ChatManager.getChatData(chatId);
        Long agentId = session.admin;
        Long requesterId = params.long("requesterId")
        String receiverName = liveChatService.getOperatorName(agentId);
        String requesterName = liveChatService.getOperatorName(requesterId);
        if (chatData) {
            AgentDataManger.removeChatFromAgentInviteRequestList(agentId, chatData.id);
            List<String> args = [receiverName.encodeAsBMHTML(), requesterName.encodeAsBMHTML()];
            ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.INVITE_CHAT_REJECT, args)
            render([status: "success"] as JSON)
        } else {
            AgentDataManger.removeChatFromAgentInviteRequestList(agentId, chatId.toLong());
            render([status: "error", errorType: "not_available", message: g.message(code: "chat.not.available")] as JSON)
        }
    }

    def sendFilePopup() {
        render(view: "/plugins/live_chat/admin/liveChat/sendFilePopup");
    }

    def sendFile() {
        String chatId = params.chatId
        ChatData chatData = ChatManager.getChatData(chatId)
        MultipartFile file = request.getFile('file')
            if (file.size <= (5 * 1024 * 1024)) {
                String tempPath = SessionManager.publicTempFolder.absolutePath
                String fileName = file.originalFilename
                fileService.uploadFile(file, null, fileName, null, tempPath)
                String fileIdentifier = ChatFileManager.pushFile(tempPath + File.separator + fileName, chatId)
                String senderName = liveChatService.getOperatorName(session.admin);
                List<String> notificationArgs = [senderName, fileName]
                Map notificationExtraFields = [
                        fileName: file.originalFilename,
                        fileIdentifier: fileIdentifier
                ]
                ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.FILE_TRANSFER, notificationArgs, notificationExtraFields)
                render([status: "success"] as JSON)
            } else {
                render([status: "error", message: g.message(code: "can.not.upload.over")] as JSON)
            }
    }

    def downloadFile() {
        String id = params.id;
        Map fileRef = ChatFileManager.getFile(id);
        if(fileRef && ChatManager.getChatData(fileRef.chatId).agentId == session.admin) {
            File file = new File(fileRef.filePath);
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "attachment; filename=\"${file.name}\"")
            InputStream inputStream = new FileInputStream(file)
            response.outputStream << inputStream
            inputStream.close()
        } else {
            render text:  g.message(code: "file.not.available")
        }
    }

    /* Tag Tab */
    @License(required = "allow_live_chat_feature")
    def loadTag() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        List<ChatTag> tags = liveChatService.getTags(params);
        Integer count = liveChatService.getTagCount(params);
        render(view: "/plugins/live_chat/admin/liveChat/loadTag", model: [tags: tags, count: count])
    }

    /* Profile Tab */
    @License(required = "allow_live_chat_feature")
    def loadProfile() {
        Map config = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT);
        Operator operatorProfile = Operator.get(session.admin);
        ChatOperatorProfile chatOperatorProfile = ChatOperatorProfile.findByOperatorId(operatorProfile.id);
        def chatDepartments = ChatDepartment.list()
        render(view: "/plugins/live_chat/admin/liveChat/loadMyProfile", model: [chatOperatorProfile : chatOperatorProfile, operatorProfile : operatorProfile, chatDepartments : chatDepartments, config : config])
    }

    @License(required = "allow_live_chat_feature")
    def saveTag() {
        def result;
        String errorMessage =  "tag.save.failed"
        try {
            result = liveChatService.saveTag(params);
        } catch (ApplicationRuntimeException ex) {
            errorMessage = ex.message
        }
        if(result) {
            render([status: "success", message: g.message(code:  "tag.save.success"), id: result] as JSON)
        } else {
            render([status: "error", message: g.message(code: errorMessage)] as JSON)
        }
    }

    def removeTag() {
        Boolean result = liveChatService.removeTag(params.long("id"));
        if(result) {
            render([status: "success", message: g.message(code:  "tag.delete.success")] as JSON)
        } else {
            render([status: "success", message: g.message(code:  "tag.delete.success")] as JSON)
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
        render(contentType: "application/json") {
            map
        }
    }

    /*Agent Tab*/
    @License(required = "allow_live_chat_feature")
    def loadAgent() {
        params.max = params.max ?: "10";
        params.offset = params.offser ?: "0";
        List agents = AgentManager.getAgents(params);
        Integer count = AgentManager.getAgentCount();
        Long myOperatorId = session.admin
        render(view: "/plugins/live_chat/admin/liveChat/loadAgent", model: [agents: agents, count: count, myOperatorId: myOperatorId]);
    }

    /* Visitor Tab */
    @License(required = "allow_live_chat_feature")
    def loadVisitor() {
        params.max = params.max ?: "10";
        params.offset = params.offser ?: "0";
        List<HttpSession> visitors = [];
        Integer count = 0;
        visitors = HookManager.hook("visitor-list", visitors);
        count = HookManager.hook("visitor-count", count);
        render(view: "/plugins/live_chat/admin/liveChat/loadVisitor", model: [visitors: visitors, count: count]);
    }

    /* Archives  tab */
    @License(required = "allow_live_chat_feature")
    def loadArchive() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        Integer count = liveChatService.getChatCount(params)
        List<Chat> chats = liveChatService.getChats(params);
        render(view: "/plugins/live_chat/admin/liveChat/loadArchive", model: [chats: chats, count: count])
    }

    def chatFilter() {
        List<Long> agentIds = ChatAgent.list().user.id;
        if(!agentIds.size()) {
            agentIds.add(0l)
        }
        render(view: "/plugins/live_chat/admin/liveChat/chatFilter", model: [agentIds: agentIds])
    }

    def viewChat() {
        Chat chat = Chat.get(params.id);
        render(view: "/plugins/live_chat/admin/liveChat/chatInfoView", model: [chat: chat])
    }

    /* Report Tab*/
    @License(required = "allow_live_chat_feature")
    def loadReport() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        String quickFilter = params.quickFilter
        if(quickFilter) {
            Date now = new Date().gmt().toZone(session.timezone);
            if(quickFilter == DomainConstants.QUICK_FILTER.TODAY) {
                params.dateFrom = now;
                params.dateTo = now;
            } else if(quickFilter == DomainConstants.QUICK_FILTER.YESTERDAY) {
                params.dateFrom = now - 1;
                params.dateTo = now - 1;
            } else if(quickFilter == DomainConstants.QUICK_FILTER.LAST_7_DAYS) {
                params.dateFrom = now - 8;
                params.dateTo = now - 1;
            } else if(quickFilter == DomainConstants.QUICK_FILTER.LAST_30_DAYS) {
                params.dateFrom = now - 31;
                params.dateTo = now - 1;
            }
        }
        Integer count = liveChatService.getChatCount(params)
        List<Chat> chats = liveChatService.getChats(params);
        render(view: "/plugins/live_chat/admin/liveChat/loadReport", model: [chats: chats, count: count])
    }

    def loadChatDepartments() {
        params.max = params.max ?: "10";
        params.offset = params.offset ?: "0";
        Integer count = liveChatService.ChatDepartments(params)
        List<ChatDepartment> chatDepartment = liveChatService.getChatDepartments(params);
        render(view: "/plugins/live_chat/admin/liveChat/config/chatDepartmentList", model: [departments: chatDepartment, count : count])
    }

    def getChatDepartmentCreatePopup() {
        def operators = Operator.list()
        render(view: "/plugins/live_chat/admin/liveChat/config/departmentCreatePopup", model: [operators: operators])
    }

    def getChatDepartmentEditPopup() {
        def operators = Operator.list()
        def chatDepartment = ChatDepartment.get(params.id.toLong())
        render(view: "/plugins/live_chat/admin/liveChat/config/departmentCreatePopup", model: [operators: operators, chatDepartment : chatDepartment])
    }

    def viewChatDepartmentDetail() {
        ChatDepartment chatDepartment = ChatDepartment.get(params.long("id"))
        render view: "/plugins/live_chat/admin/liveChat/config/chatDepartmentDetail", model: [chatDepartment: chatDepartment]
    }

    def saveOrUpdateChatDepartment() {
        ChatDepartment chatDepartment = ChatDepartment.get(params.id)
        if(chatDepartment){
           updateChatDepartment(chatDepartment);
        } else{
            saveChatDepartment();
        }
    }

    def saveChatDepartment() {
        def result;
        String errorMessage =  "department.save.failed"
        try {
            List<Long> ids = params.list("operators")*.toLong();
            result = liveChatService.saveChatDepartment(params, ids);
        } catch (ApplicationRuntimeException ex) {
            errorMessage = ex.message
        }
        if(result) {
            render([status: "success", message: g.message(code:  "department.save.success"), id: result] as JSON)
        } else {
            render([status: "error", message: g.message(code: errorMessage)] as JSON)
        }
    }

    def updateChatDepartment(ChatDepartment chatDepartment) {
        def result;
        String errorMessage =  "department.update.failed"
        try {
            List<Long> ids = params.list("operators")*.toLong();
            chatDepartment.name = params.name;
            chatDepartment.description = params.description;
            chatDepartment.defaultWelcomeMessage = params.defaultWelcomeMessage;
            result = liveChatService.updateChatDepartment(chatDepartment, ids);
        } catch (ApplicationRuntimeException ex) {
            errorMessage = ex.message
        }
        if(result) {
            render([status: "success", message: g.message(code:  "department.update.success"), id: result] as JSON)
        } else {
            render([status: "error", message: g.message(code: errorMessage)] as JSON)
        }
    }

    def removeChatDepartment() {
        Boolean result = liveChatService.removeChatDepartment(params.id.toLong());
        if(result) {
            render([status: "success", message: g.message(code:  "department.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code:  "department.delete.failed")] as JSON)
        }
    }

    def updateOperatorProfile() {
        def result;
        String errorMessage =  "operator.profile.update.failed"
        def uploadedFile = request.getFile("profileImage")
        try {
            params.operatorId  = session.admin
            result = liveChatService.updateOperatorProfile(params, uploadedFile);
        } catch (ApplicationRuntimeException ex) {
            errorMessage = ex.message
        }
        if(result) {
            render([status: "success", message: g.message(code:  "operator.profile.update.success"), id: result] as JSON)
        } else {
            render([status: "error", message: g.message(code: errorMessage)] as JSON)
        }
    }

    def startChatWithOperator() {
        ChatOperatorProfile chatOperatorProfile = ChatOperatorProfile.findByOperatorId(session.admin)
        if(chatOperatorProfile){
             params.name = chatOperatorProfile.displayName
        } else{
            Operator operatorProfile = Operator.get(session.admin)
            params.name = operatorProfile.fullName
        }
        ChatData chatData = liveChatService.initChatWithAnotherOperator(params);
        AgentDataManger.addChatToAgentAttendedList(params.agentId.toLong(), chatData)
        AgentDataManger.addChatToAgentAttendedList(session.admin, chatData)
        render([status: "success", message: g.message(code:  "chat.started")] as JSON)
    }

    def sendImmediatelyChatToMailPopup() {
        render(view: "/plugins/live_chat/admin/liveChat/sendImmediatlyChatToMailPopup.gsp", model: [chatId: params.chatId])
    }

    def sendImmediatelyChatToMail() {
        Chat chat = Chat.get(params.chatId);
        liveChatService.sendChatToMail(chat, params.recipient)
        render([status: "success", message: g.message(code: "transcript.send.success")] as JSON)
    }

    @Transactional
    def updateChat() {
        Chat chat = Chat.get(params.id);
        if(chat){
            chat.name = params.name;
            chat.phone = params.phone;
            chat.email = params.email;
            if(params.department){
                chat.chatDepartment = ChatDepartment.get(params.department);
            }
            chat.note = params.note;
            chat.save();
        }
        ChatData chatData = ChatManager.getChatData(params.id);
        if(chatData){
            chatData.name = params.name;
            chatData.email = params.email;
            ChatManager.pushChat(params.id, chatData);
        }
        render([status: "success", message: g.message(code: "chat.update.success")] as JSON)
    }

    def exportChat() {
        CsvListWriter listWriter = null
        try {
            response.setHeader("Content-Type", "text/text");
            response.setHeader("Content-Disposition", "attachment; filename=chat_"+params.chatId+".txt")
            OutputStreamWriter writer = new OutputStreamWriter(response.outputStream)
            listWriter = new CsvListWriter(writer, CsvPreference.TAB_PREFERENCE)
            List<String> fieldValueList = []
            Chat chat = Chat.get(params.chatId);
            if(!chat.isComplete){
                ChatData chatData = ChatManager.getChatData(params.chatId);
                chatData.messages.each{
                    fieldValueList.add(it.name);
                    fieldValueList.add(it.message);
                    listWriter.write(fieldValueList)
                }
            } else{
                chat.messages.each{
                    fieldValueList.add(it.name);
                    fieldValueList.add(it.message);
                    listWriter.write(fieldValueList)
                }
            }

        } finally {
            if( listWriter != null ) {
                listWriter.close()
            }
        }

    }
}

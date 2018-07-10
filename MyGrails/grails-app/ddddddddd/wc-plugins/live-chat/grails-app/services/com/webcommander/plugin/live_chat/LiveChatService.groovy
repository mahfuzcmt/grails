package com.webcommander.plugin.live_chat

import com.webcommander.admin.Operator
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants as DC
import com.webcommander.events.AppEventManager
import com.webcommander.parser.EmailTemplateParser
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.AgentDataManger
import com.webcommander.plugin.live_chat.manager.AgentManager
import com.webcommander.plugin.live_chat.manager.ChatFileManager
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.plugins.web.taglib.RenderTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Initializable
class LiveChatService {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.RenderTagLib")
    RenderTagLib g;
    CommanderMailService commanderMailService
    CommonService commonService

    static {
        AppEventManager.on("session-terminate", { session ->
            LiveChatService service = Holders.applicationContext.getBean(LiveChatService);
            Long admin = session.admin
            if (admin) {
                if (AgentManager.getLoginCountByAgent(admin) <= 1 && !AgentDataManger.deviceCountByAgent(admin)) {
                    List<ChatData> attendedChats = AgentDataManger.getAgentAttendedList(admin).clone();
                    attendedChats.each {
                        service.leaveChatByAdmin(it);
                    }
                }
                AgentManager.removeAgent(session.admin, session.id);
            }
            ChatData chatData = ChatManager.getChatData(session.live_chat_ref);
            if (chatData) {
                service.leaveChat(chatData)
            }
        });

        AppEventManager.on("admin-logged-in", { userId ->
            ChatAgent chatAgent = ChatAgent.where {
                user.id == userId
            }.get();
            if(chatAgent && chatAgent.isActive) {
                AgentManager.pushAgent(userId);
            }
        });

        AppEventManager.on("admin-logged-out", { id, session ->
            if(id) {
                if (AgentManager.getLoginCountByAgent(id) <= 1 && !AgentDataManger.deviceCountByAgent(id)) {
                    LiveChatService service = Holders.applicationContext.getBean(LiveChatService)
                    List<ChatData> attendedChats = AgentDataManger.getAgentAttendedList(id).clone();
                    attendedChats.each {
                        service.leaveChatByAdmin(it);
                    }
                }
                AgentManager.removeAgent(id, session.id);
            }
        });
    }

    static void initialize() {
        AppEventManager.on("before-delete-chat-tag", { id ->
            ChatTag chatTag = ChatTag.get(id)
            Chat.createCriteria().list {
                tags {
                    eq("id", id)
                }
            }.each {
                it.removeFromTags(chatTag);
                it.save();
            }

        });

        AppEventManager.on("before-operator-delete", {id ->
            ChatAgent.createCriteria().get {
                eq("user.id", id)
            }?.delete();
        });
    }

    String getGreetingBasedOnTime() {
        Calendar calendar = Calendar.getInstance(AppUtil.session.timezone);
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting = "good.evening";
        if (hour >= 6 && hour < 12) {
            greeting = "good.morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "good.afternoon";
        }
        return g.message(code: greeting);
    }

    @Transactional
    def initChat(Map params) {
        Chat chat = new Chat();
        chat.name = params.name;
        chat.email = params.email
        chat.phone = params.phone
        chat.subject = params.subject;
        chat.save();
        ChatData chatData = new ChatData(chat.id, chat.name, chat.email, new Date().gmt().getTime());
        String chatId = "" + chat.id
        ChatManager.pushChat(chatId, chatData);
        AgentDataManger.addChatToOrphans(chatData);
        if(params.message) {
            ChatManager.sendChatMessage(chatId, params.message, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)
        }
        String welcomeMessage = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT, "welcome_message");
        if(welcomeMessage) {
            Map macros = [
                    customer_name: chat.name.encodeAsBMHTML(),
                    time_greetings: getGreetingBasedOnTime()
            ]
            welcomeMessage = EmailTemplateParser.parse(welcomeMessage, macros);
            ChatManager.sendChatMessage(chatId, welcomeMessage, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT)
        }
        AppUtil.session.live_chat_ref = chatId
        return chatData
    }

    Boolean isTerminated(Long chatId) {
        Chat chat = Chat.get(chatId);
        return chat ? chat.isComplete : true
    }



    @Transactional
    def rateChat(Map params) {
        String chatId = AppUtil.session.live_chat_ref
        Chat chat = Chat.get(chatId);
        chat.rating = params.rating;
        chat.save();
        String notificationType
        if(params.rating == DomainConstants.CHAT_RATING_TYPE.UP) {
           notificationType = DomainConstants.NOTIFICATION_TYPE.RATE_CHAT_GOOD;
        } else if (params.rating == DomainConstants.CHAT_RATING_TYPE.DOWN) {
           notificationType = DomainConstants.NOTIFICATION_TYPE.RATE_CHAT_BAD;
        } else {
           notificationType = DomainConstants.NOTIFICATION_TYPE.RATIND_CANCEL;
        }
        List notificationArgs = [chat.name];
        if(chat.hasErrors()) {
            return false;
        }
        ChatManager.addNotificationToChat(chatId, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER, notificationType, notificationArgs)
        return true;
    }

    @Transactional
    void saveChatMessages(ChatData chatData) {
        Chat chat = Chat.get(chatData.id);
        chatData.messages.each {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.senderType = it.senderType;
            chatMessage.message = it.message;
            chatMessage.isNotification = it.isNotification;
            if(it.isNotification) {
                chatMessage.notificationType = it.notificationType;
                it.notificationArgs.each {
                    chatMessage.addToNotificationArgs(it);
                }
            }
            chatMessage.created = new Date(it.time);
            chatMessage.chat = chat
            chatMessage.save();
            if(it.fileIdentifier) {
                ChatFileManager.removeFile(it.fileIdentifier)
            }
        }
        chat.isComplete = true;
        chat.agentId = chatData.agentId;
        chat.save();
    }

    void leaveChat(ChatData chatData) {
        saveChatMessages(chatData);
        if(chatData.historyRecipient) {
            Thread.start {
                Chat.withNewSession {
                    AppUtil.initialDummyRequest();
                    Chat chat = Chat.get(chatData.id)
                    sendChatToMail(chat, chatData.historyRecipient)
                }
            }
        }
        if(chatData.agentId) {
            AgentDataManger.removeChatFromAgentAttendedList(chatData.agentId, chatData)
        } else {
            AgentDataManger.removeChatFromOrphans(chatData);
        }
        ChatManager.removeChat(chatData.id + "");
    }

    void leaveChatByAdmin(ChatData chatData) {
        List<String> notificationArgs = ["Admin"];
        ChatManager.addNotificationToChat(chatData, DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT, DomainConstants.NOTIFICATION_TYPE.LEAVE_CHAT, notificationArgs)
        leaveChat(chatData)
    }

    Closure getChatCriteriaClosure(Map params) {
        def session = AppUtil.session
        return {
            if (params.customerName) {
                ilike("name", "%${params.customerName.trim().encodeAsLikeText()}%")
            }
            if (params.phone) {
                eq("phone", params.phone)
            }
            if (params.dateFrom) {
                Date date = params.dateFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.dateTo) {
                Date date = params.dateTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.tag) {
                tags {
                    eq("id", params.tag.toLong())
                }
            }
            if(params.rating) {
                eq("rating", params.rating)
            }
            if(params.agentId) {
                eq("agentId", params.long("agentId"))
            }
            if(params.searchText) {
                inList "id", Chat.where {
                    projections {
                        distinct("id")
                    }
                    messages {
                        ilike("message", "%${params.searchText.trim().encodeAsLikeText()}%")
                    }
                }
            }
            eq("isComplete", true)
        }
    }

    List<Chat> getChats(Map params) {
        return Chat.createCriteria().list(max: params.max, offset: params.offset) {
            and getChatCriteriaClosure(params)
            order("id", "desc")
        }
    }

    Integer getChatCount(Map params) {
        return Chat.createCriteria().count {
            and getChatCriteriaClosure(params)
        }
    }

    def sendOfflineMessage(Map params) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("live-chat-offline-email")
        if(!macrosAndTemplate.emailTemplate.active) {
            return false;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name":
                    refinedMacros[it.key] = params.name.encodeAsBMHTML();
                    break;
                case "customer_email":
                    refinedMacros[it.key] = params.email;
                    break
                case "customer_phone":
                    refinedMacros[it.key] = params.phone;
                    break
                case "message":
                    refinedMacros[it.key] = params.message.encodeAsBMHTML();
                    break;
                case "subject":
                    refinedMacros[it.key] = params.subject.encodeAsBMHTML();
                    break;
            }
        }
        String recipient = AppUtil.getConfig(DC.SITE_CONFIG_TYPES.LIVE_CHAT, "offline_message_recipient")
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient);
    }

    def sendChatToMail(Chat chat, recipient) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("send-chat-to-mail")
        if(!macrosAndTemplate.emailTemplate.active) {
            return false;
        }
        List supportedNotifications = ["terminate_chat", "rate_chat_good", "rate_chat_bad", "rating_cancel", "leave_chat"];
        Map refinedMacros = macrosAndTemplate.commonMacros
        def messages = chat.messages;
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "start_time":
                    refinedMacros[it.key] = chat.created.toEmailFormat();
                    break;
                case "end_time":
                    refinedMacros[it.key] = chat.updated.toEmailFormat();
                    break
                case "duration":
                    def diff = chat.updated.getTime() - chat.created.getTime();
                    long seconds = diff / 1000
                    seconds = seconds % 60;
                    long minutes = diff / (60 * 1000)
                    minutes = minutes % 60;
                    long hours = diff / (60 * 60 * 1000)
                    refinedMacros[it.key] =  hours + ":" + minutes + ":" + seconds;
                    break;
                case "visitor_name":
                    refinedMacros[it.key] = chat.name == "Guest" ? "Guest-" + chat.id : chat.name.encodeAsBMHTML();
                    break;
                case "visitor_email":
                    refinedMacros[it.key] = chat.email ?: "";
                    break;
                case "visitor_phone":
                    refinedMacros[it.key] = chat.phone ?: "";
                    break;
                case "agent_name":
                    Operator user = chat.agentId ? Operator.get(chat.agentId) : null;
                    refinedMacros[it.key] = user ? user.fullName.encodeAsBMHTML() : "";
                    break;
                case "messages":
                    List modifiedMessages = []
                    messages.each {
                       Map message = [:];
                       message.sender_type = it.senderType
                       message.sender_name = it.senderType == DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER ? chat.name : "Admin";
                       message.is_notification = it.isNotification;
                       message.is_not_notification = !it.isNotification;
                       message.time = it.created.toEmailFormat();
                       if(it.isNotification) {
                           if(it.notificationType in supportedNotifications) {
                               message.message = g.message(code: "notification." + it.notificationType + ".for.customer", args: it.notificationArgs);
                               modifiedMessages.add(message);
                           }
                       } else {
                           message.message = it.message
                           modifiedMessages.add(message);
                       }
                    }
                    refinedMacros[it.key] = modifiedMessages
                    break;
            }
        }
        commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient);
    }

    Boolean isActivated(Long userId) {
        Operator user = Operator.get(userId);
        ChatAgent agent = ChatAgent.findByUser(user);
        return agent != null && agent.isActive
    }

    @Transactional
    Boolean activateChat(Long userId) {
        Operator user = Operator.get(userId);
        ChatAgent agent = ChatAgent.findByUser(user);
        if(!agent) {
            agent = new ChatAgent();
            agent.user = user
        }
        agent.isActive = true
        agent.save();
        if(!agent.hasErrors()) {
            AgentManager.pushAgent(userId);
            return true
        }
        return false;
    }

    @Transactional
    Boolean deactivateChat(Long userId) {
        Operator user = Operator.get(userId);
        ChatAgent agent = ChatAgent.findByUser(user);
        agent.isActive = false
        agent.save()
        if(!agent.hasErrors()) {
            List<ChatData> attendedChats = AgentDataManger.getAgentAttendedList(userId).clone();
            attendedChats.each {
                leaveChatByAdmin(it);
            }
            AgentManager.removeAgent(userId)
            return true
        }
        return false
    }
    /*Chat Tab*/
    @Transactional
    Boolean addTagToChat(Map params) {
        Chat chat = Chat.get(params.chatId);
        ChatTag tag = ChatTag.get(params.tagId);
        chat.addToTags(tag);
        chat.save();
        return !chat.hasErrors();
    }

    @Transactional
    Boolean removeTagFromChat(Map params) {
        Chat chat = Chat.get(params.chatId);
        ChatTag tag = ChatTag.get(params.tagId);
        chat.removeFromTags(tag);
        chat.save();
        return !chat.hasErrors();
    }

    /* Tag Tab*/
    List<ChatTag> getTags(Map params) {
        return ChatTag.createCriteria().list(max: params.max, offset: params.offset) {
            order("name", params.dir ?: "asc")
        }
    }

    Integer getTagCount(Map params) {
        ChatTag.createCriteria().count() {
        }
    }

    @Transactional
    Long saveTag(Map params) {
        ChatTag chatTag = params.id ? ChatTag.get(params.id) : new ChatTag();
        if (!commonService.isUnique(ChatTag, [id: params.id, field: "name", value: params.name])) {
            throw new ApplicationRuntimeException("tag.name.exists")
        }
        chatTag.name = params.name;
        chatTag.save();
        return chatTag.id;
    }

    @Transactional
    Boolean removeTag(Long id) {
        AppEventManager.fire("before-delete-chat-tag", [id]);
        ChatTag chatTag = ChatTag.get(id)
        chatTag.delete()
        return true;
    }
}

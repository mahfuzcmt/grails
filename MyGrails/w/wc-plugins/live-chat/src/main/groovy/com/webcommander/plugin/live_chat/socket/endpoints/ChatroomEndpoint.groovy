package com.webcommander.plugin.live_chat.socket.endpoints

import com.webcommander.plugin.live_chat.LiveChatService
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.plugin.live_chat.socket.config.EndpointConfigurator
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSession
import javax.websocket.*
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value = "/socket/chat/{chatId}", configurator = EndpointConfigurator.class)
@WebListener
class ChatroomEndpoint implements ServletContextListener {
    private HttpSession session;
    private static final Logger log = Logger.getLogger(ChatroomEndpoint.class)
    static final Set<Session> users = ([] as Set).asSynchronized()

    @Override
    void contextInitialized(ServletContextEvent servletContextEvent) {}

    @Override
    void contextDestroyed(ServletContextEvent servletContextEvent) {
    }


    @OnOpen
    public void onOpen(Session userSession, EndpointConfig config, @PathParam("chatId") final String chatId) {
        userSession.userProperties.put("chatId", chatId);
        this.session = (HttpSession) config.userProperties.get(HttpSession.class.getName());
        ChatData chatData = ChatManager.getChatData(chatId)
        Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
        def chatConfig = JsonOutput.toJson(liveChatConfig)
        chatData ? chatData.sessions.add(userSession) : null
        if(chatData){
            chatData.sessions.each{
                it.getBasicRemote().sendText(chatConfig)
            }
        }
    }


    @OnMessage
    @Transactional
    public void onMessage(String message, Session userSession) {
        String chatId = userSession.userProperties.get("chatId")
        if(chatId) {
            def messageObject = new JsonSlurper().parseText(message)
            if(messageObject){
                def liveChatService =  Holders.grailsApplication.mainContext.getBean(LiveChatService)
                String agentName = null
                if(session.admin){
                    agentName = liveChatService.getOperatorName(session.admin)
                }
                ChatManager.sendChatMessageFromSocket(agentName, chatId, messageObject.message, messageObject.user == 'agent' ? DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT : DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)
            }
            ChatData chatData =  ChatManager.CHAT_HOLDER.get(chatId)
            Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
            def chatConfig = JsonOutput.toJson(liveChatConfig)
            chatData.sessions.each{
                it.getBasicRemote().sendText(chatConfig)
            }
        }
    }


    @OnClose
    public void onClose(Session userSession, CloseReason closeReason) {
        String chatId = userSession.userProperties.get("chatId");
        ChatData chatData = ChatManager.getChatData(chatId);
        if(chatData) {
            Map liveChatConfig = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LIVE_CHAT);
            def chatConfig = JsonOutput.toJson(liveChatConfig)
            chatData.sessions.remove(userSession);
            chatData.sessions.each{
                it.getBasicRemote().sendText(chatConfig)
            }
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error(t.message, t)
    }

}
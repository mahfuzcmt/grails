package com.webcommander.plugin.live_chat.socket.endpoints

import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.manager.ChatManager
import com.webcommander.plugin.live_chat.models.ChatData
import com.webcommander.plugin.live_chat.socket.config.EndpointConfigurator
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
        if(this.session.getAttribute('live_chat_ref')) {
            ChatData chatData = ChatManager.getChatData(chatId)
            chatData ? chatData.sessions.add(userSession) : null
        }
    }


    @OnMessage
    public void onMessage(String message, Session userSession) {
        def messageObject = new JsonSlurper().parseText(message)
        String chatId = userSession.userProperties.get("chatId")
        if(chatId) {
            ChatManager.sendChatMessage(chatId, messageObject.message, messageObject.user == 'agent' ? DomainConstants.CHAT_MESSAGE_SENDER_TYPE.AGENT : DomainConstants.CHAT_MESSAGE_SENDER_TYPE.CUSTOMER)
        }
    }


    @OnClose
    public void onClose(Session userSession, CloseReason closeReason) {
        String chatId = userSession.userProperties.get("chatId");
        ChatData chatData = ChatManager.getChatData(chatId);
        if(chatData) {
            chatData.sessions.remove(userSession);
        }
    }

    @OnError
    public void onError(Throwable t) {
        log.error(t.message, t)
    }

}
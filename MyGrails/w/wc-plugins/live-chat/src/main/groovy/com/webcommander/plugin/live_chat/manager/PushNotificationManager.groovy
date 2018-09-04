package com.webcommander.plugin.live_chat.manager

import com.webcommander.admin.Operator
import com.webcommander.manager.PathManager
import com.webcommander.plugin.live_chat.constants.DomainConstants
import com.webcommander.plugin.live_chat.models.ChatData
import grails.util.Holders
import javapns.Push
import javapns.devices.Device
import javapns.notification.PushNotificationPayload
import org.springframework.context.MessageSource

/**
 * Created by sajedur on 14-12-2014.
 */
class PushNotificationManager {

    static Boolean IS_PRODUCTION = false
    static String PASSWORD = "webcommanderiapp"
    static File CERTIFICATE_FILE = new File(PathManager.getRestrictedResourceRoot("certificate/iAppCertificate.p12"))

    private static MessageSource _messageSource
    private static getMessageSource() {
        if(_messageSource) {
            return _messageSource
        }
        return _messageSource = Holders.applicationContext.getBean('messageSource')
    }
    private static PushNotificationPayload buildPayload(String body, Map data = [:]) {
        String rawJson = "{ 'aps': { 'alert': { 'body': '${body}' }, 'badge': 1, 'sound': 'default' }"
        data.each {
            rawJson = rawJson + ", '${it.key}': '${it.value}'"
        }
        rawJson = rawJson + "}";
        return new PushNotificationPayload(rawJson);
    }

    static void notifyForOrphanChat(ChatData chatData) {
        List<Device> devices = AgentDataManger.getAllDevice();
        if(devices.size()) {
            Map data = [
                chatId: chatData.id,
                type: DomainConstants.PUSH_NOTIFICATION_TYPE.NEW_CHAT
            ]
            PushNotificationPayload payload = buildPayload("New Chat from ${chatData.name}", data)
            Push.payload(payload, CERTIFICATE_FILE, PASSWORD, IS_PRODUCTION, devices)
        }
    }

    static void notifyForNewChatMessage(ChatData chatData) {
        List<Device> devices = chatData.agentId ? AgentDataManger.getInactiveDevicesByAgent(chatData.agentId) : [];
        if(devices.size()) {
            Map data = [
                    chatId: chatData.id,
                    type: DomainConstants.PUSH_NOTIFICATION_TYPE.NEW_MESSAGE
            ]
            PushNotificationPayload payload = buildPayload("New message from ${chatData.name}", data)
            Push.payload(payload, CERTIFICATE_FILE, PASSWORD, IS_PRODUCTION, devices)
        }
    }

    static void notifyForTransferRequest(ChatData chatData, Long agentId) {
        List<Device> devices = AgentDataManger.getDevicesByAgent(agentId)
        if(devices.size()) {
            Map data = [
                chatId: chatData.id,
                type: DomainConstants.PUSH_NOTIFICATION_TYPE.TRNSFER_REQUEST
            ]
            PushNotificationPayload payload = buildPayload("New transfer request from ${Operator.get(agentId).fullName}", data)
            Push.payload(payload, CERTIFICATE_FILE, PASSWORD, IS_PRODUCTION, devices)
        }
    }

    static void notifyForInviteRequest(ChatData chatData, Long agentId) {
        List<Device> devices = AgentDataManger.getDevicesByAgent(agentId)
        if(devices.size()) {
            Map data = [
                    chatId: chatData.id,
                    type: DomainConstants.PUSH_NOTIFICATION_TYPE.INVITE_REQUEST
            ]
            PushNotificationPayload payload = buildPayload("New invite request from ${Operator.get(agentId).fullName}", data)
            Push.payload(payload, CERTIFICATE_FILE, PASSWORD, IS_PRODUCTION, devices)
        }
    }

    static void notifyForNotification(ChatData chatData, notificationType) {
        List<Device> devices = chatData.agentId ? AgentDataManger.getInactiveDevicesByAgent(chatData.agentId) : [];
        if(devices.size()) {
            Map data = [
                chatId: chatData.id,
                type: DomainConstants.PUSH_NOTIFICATION_TYPE.NOTIFICATION
            ]
            String code = "notification.${notificationType}.for.admin";
            String body = messageSource.getMessage(code , [chatData.name] as Object[], code, Locale.default)
            PushNotificationPayload payload = buildPayload(body, data)
            Push.payload(payload, CERTIFICATE_FILE, PASSWORD, IS_PRODUCTION, devices)
        }
    }

}

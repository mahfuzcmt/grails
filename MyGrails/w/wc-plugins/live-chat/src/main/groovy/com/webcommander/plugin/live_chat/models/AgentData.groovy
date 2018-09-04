package com.webcommander.plugin.live_chat.models

import com.webcommander.admin.Operator
import com.webcommander.plugin.live_chat.manager.ChatManager

class AgentData {

    private List<ChatData> attendedChats
    private List<Map> transferRequests
    private List<Map> inviteRequests
    private List<WcDevice> deviceList

    public AgentData() {
        this.attendedChats = new ArrayList<ChatData>();
        this.transferRequests = new ArrayList<Map>();
        this.inviteRequests = new ArrayList<Map>();
        this.deviceList = new ArrayList<WcDevice>()
    }

    Boolean addToAttendedChats(ChatData chatData) {
        if(!attendedChats.find { it == chatData}) {
            return attendedChats.add(chatData);
        }
        return false
    }

    Boolean removeFromAttendedChats(ChatData chatData) {
        return attendedChats.removeAll {
            it == chatData
        };
    }

    Boolean addToTransferRequests(Long chatId, Long requesterId, String message) {
        ChatData chatData = ChatManager.getChatData(chatId + "");
        Map transferRequest = [
            chatId: chatId,
            visitorName: chatData.name,
            requesterId: requesterId,
            requsterName: Operator.get(requesterId).fullName,
            message: message
        ]
        return transferRequests.add(transferRequest);
    }

    Boolean removeFromTransferRequests(Long chatId) {
        return transferRequests.removeAll {
            it.chatId == chatId
        };
    }

    Boolean addToInviteRequests(Long chatId, Long requesterId, String message) {
        ChatData chatData = ChatManager.getChatData(chatId + "");
        Map transferRequest = [
                chatId: chatId,
                visitorName: chatData.name,
                requesterId: requesterId,
                requsterName: Operator.get(requesterId).fullName,
                message: message
        ]
        return inviteRequests.add(transferRequest);
    }

    Boolean removeFromInviteRequests(Long chatId) {
        return inviteRequests.removeAll {
            it.chatId == chatId
        };
    }

    WcDevice getDevice(String deviceId) {
        return deviceList.find {
            it.deviceId == deviceId
        }
    }

    WcDevice addDevice(WcDevice device) {
        return deviceList.add(device) ? device : null;
    }

    WcDevice addDevice(String deviceId, Boolean isActive = true) {
        WcDevice device = this.getDevice(deviceId)
        if(device) {
            return device;
        }
        device = new WcDevice(deviceId, isActive);
        return addDevice(device)
    }

    Boolean removeDevice(WcDevice device) {
        return deviceList.remove(device)
    }

    Boolean removeDevice(String deviceId) {
        WcDevice device = this.getDevice(deviceId);
        return removeDevice(device)
    }

    WcDevice setDeviceStatus(String deviceId, Boolean isActive) {
        WcDevice device = this.getDevice(deviceId)
        device.isActive = isActive
        return device
    }

    List<WcDevice> getInactiveDevices() {
        return deviceList.findAll {
            it.isActive == false
        }
    }

    List<WcDevice> getDevices() {
        return deviceList
    }

    List<ChatData> getAttendedChats() {
        return attendedChats
    }

}

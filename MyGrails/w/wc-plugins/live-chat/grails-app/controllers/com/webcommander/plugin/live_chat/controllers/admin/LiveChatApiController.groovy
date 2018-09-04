package com.webcommander.plugin.live_chat.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.plugin.live_chat.manager.AgentDataManger
import com.webcommander.plugin.live_chat.models.AgentData
import com.webcommander.util.AppUtil
import grails.converters.JSON
import javapns.devices.Device

class LiveChatApiController {

    @License(required = "allow_live_chat_feature")
    def startNotification() {
        AgentData agentData = AgentDataManger.getAgentData(AppUtil.loggedOperator);
        String deviceToken = params.token ?: session.device_token;
        Device device = agentData.setDeviceStatus(deviceToken, false);
        if(device) {
            render([status: "success"] as JSON)
        } else {
            render([status: "success"] as JSON)
        }
    }

    @License(required = "allow_live_chat_feature")
    def stopNotification() {
        AgentData agentData = AgentDataManger.getAgentData(AppUtil.loggedOperator);
        String deviceToken = params.token ?: session.device_token;
        Device device = agentData.setDeviceStatus(deviceToken, true);
        if(device) {
            render([status: "success"] as JSON)
        } else {
            render([status: "success"] as JSON)
        }
    }
}

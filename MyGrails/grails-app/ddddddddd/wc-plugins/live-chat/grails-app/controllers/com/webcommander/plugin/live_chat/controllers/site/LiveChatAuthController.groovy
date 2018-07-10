package com.webcommander.plugin.live_chat.controllers.site

import com.webcommander.admin.Operator
import com.webcommander.common.AuthenticationService
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.live_chat.ChatAgent
import com.webcommander.plugin.live_chat.manager.AgentDataManger
import com.webcommander.plugin.live_chat.models.AgentData
import com.webcommander.plugin.live_chat.models.WcDevice
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import javapns.devices.exceptions.InvalidDeviceTokenFormatException

import javax.servlet.http.Cookie

class LiveChatAuthController {
    AuthenticationService authenticationService

    def login() {
        try {
            WcDevice device = new WcDevice(params.token, true);
            Operator user = authenticationService.verifyUser(params.userName, params.password);
            ChatAgent agent = user ? ChatAgent.findByUser(user) : null;
            if(agent) {
                session.admin = user.id;
                session.device_token = device.deviceId
                AgentData agentData = AgentDataManger.getAgentData(user.id);
                if(!agentData.getDevice(device.deviceId)) {
                    agentData.addDevice(device)
                }
                AppEventManager.fire("admin-logged-in", [user.id])
                if (params.remember) {
                    InformationEncrypter rsa = new InformationEncrypter();
                    rsa.hideInfo(params.userName)
                    rsa.hideInfo(params.password)
                    String key = (System.currentTimeMillis() + "").encodeAsMD5().encodeAsBase64() + "^" + rsa.toString();
                    Cookie cookie = new Cookie("remember-admin", key);
                    cookie.maxAge = 604800;
                    cookie.path = "/";
                    response.addCookie(cookie);
                } else {
                    Cookie cookie = request.getCookies().find {it.name.startsWith("remember-admin")};
                    if(cookie) {
                        cookie.maxAge = 0;
                        response.addCookie(cookie);
                    }
                }
                render([status: "success"] as JSON)
            } else {
                render([status: "error"] as JSON)
            }
        } catch (InvalidDeviceTokenFormatException e) {
            render([status: "error", message: e.message] as JSON)
        }
    }

    def logout() {
        AgentData agentData = AgentDataManger.getAgentData(session.admin);
        agentData.removeDevice(params.token ?: session.device_token)
        authenticationService.clearAdminSession(session);
        Cookie adminRemember = request.getCookies().find {it.name.startsWith("remember-admin")};
        if (adminRemember) {
            adminRemember.maxAge = 0;
            adminRemember.path = "/";
            response.addCookie(adminRemember);
        }
        render([status: "success"] as JSON)
    }
}

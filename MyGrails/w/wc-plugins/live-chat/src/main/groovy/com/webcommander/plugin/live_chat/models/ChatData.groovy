package com.webcommander.plugin.live_chat.models

import javax.websocket.Session

/**
 * Created by sajedur on 23/10/2014.
 */
class ChatData {
    Long id
    Long agentId
    Long enterTime
    String name
    String email
    String historyRecipient
    int numbersOfIdleAlertSent

    Long lastRead

    List<Map> messages;
    List<Session> sessions;

    public ChatData(Long id, String name, String email, enterTime) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.enterTime = enterTime
        messages = [];
        sessions = [];
        numbersOfIdleAlertSent = 0;
    }
}

package com.webcommander.plugin.live_chat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.util.concurrent.TimeUnit

class LiveChatTagLib {
    static namespace = "liveChat"

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def adminJss = { attr, body ->
        out << body()
        out << app.javascript(src: 'plugins/live-chat/js/admin/live-chat-manager.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/sub-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/chat-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/tag-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/archive-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/report-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/live-chat-settings-ext.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/profile-tab.js')
        out << app.javascript(src: 'plugins/live-chat/js/admin/agent-tab.js')
    }

    def siteJSs = { attrs, body ->
        out << body();
        if(params.editMode != "true" && params.viewMode != "true") {
            out << app.javascript(src: 'plugins/live-chat/js/site-js/live-chat.js')
        }
    }


    def toDuration = { attr, body ->
        Long millis = attr.millis
        long SECOND = 1000;
        long MINUTE = 60 * SECOND
        out << TimeUnit.MILLISECONDS.toMinutes(millis) + "m "
        millis = millis % MINUTE
        out << TimeUnit.MILLISECONDS.toSeconds(millis) + "s"
    }
}

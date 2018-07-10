package com.webcommander.plugin.live_chat.mixin_service

import com.webcommander.plugin.live_chat.manager.AgentManager
import com.webcommander.widget.Widget
import grails.converters.JSON

class WidgetService {

    def populateLiveChatInitialContentNConfig(Widget widget) {
        widget.title = "s:need.help"
        widget.params = ([online_text: "s:chat.with.us", offline_text: "s:send.us.message"] as JSON).toString();
    }

    def renderLiveChatWidget(Widget widget, Writer writer) {
        def config = JSON.parse(widget.params)
        Boolean hasAgent = AgentManager.agentCount > 0;
        renderService.renderView("/plugins/live_chat/site/widget/liveChat", [widget: widget, config: config, hasAgent: hasAgent], writer)
    }
}

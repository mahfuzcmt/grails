package com.webcommander.plugin.live_chat.controllers.admin.design


class WidgetController {


    def liveChatShortConfig() {
        render(view: "/plugins/live_chat/admin/widget/shortConfig", model: [noAdvance: true]);
    }
}

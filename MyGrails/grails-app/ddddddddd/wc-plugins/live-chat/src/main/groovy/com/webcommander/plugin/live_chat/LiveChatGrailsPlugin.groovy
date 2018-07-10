package com.webcommander.plugin.live_chat

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta


class LiveChatGrailsPlugin extends WebCommanderPluginBase {

    def title = "Live Chat"
    def author = "Md Sajedur Rahman"
    def authorEmail = "sajedur@bitmascot.com"
    def description = '''LiveChat Description'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/live-chat";
    {
        _plugin = new PluginMeta(identifier: "live-chat", name: title)
        hooks=[layoutHead:[taglib:"liveChat",callable:"siteJSs"],adminJss:[taglib:"liveChat",callable:"adminJss"]]
    }


}

package com.webcommander.plugin.live_chat

class ChatTag {
    String name

    static constraints = {
        name(unique: true)
    }
}

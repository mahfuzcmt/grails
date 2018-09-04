package com.webcommander.plugin.live_chat

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class LiveChatResourceTagLib {

    static namespace = "appResource"

    public static final RESOURCES_PATH = [
            "OPERATOR" : "operator"
    ]

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    def getLiveChatOperatorImageRelativeUrl(def id) {
        return "${RESOURCES_PATH.OPERATOR}/operator-$id/"
    }

    def getChatProfileImageUrl = { attrs, body->
        def profile = attrs.profileImage
        String sizeOrPrefix = attrs.sizeOrPrefix ?: ""
        String url = profile.profileImage ? parent.getAbstractStaticResourceImageURL(profile, sizeOrPrefix) : parent.getDefaultImageWithPrefix(sizeOrPrefix, parent.PRODUCT)
        out << url
    }
}
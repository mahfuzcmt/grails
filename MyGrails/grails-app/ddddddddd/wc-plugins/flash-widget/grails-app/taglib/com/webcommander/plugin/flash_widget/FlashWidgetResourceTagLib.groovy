package com.webcommander.plugin.flash_widget

import com.webcommander.AppResourceTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class FlashWidgetResourceTagLib {
    static namespace = "appResource"

    @Autowired
    @Qualifier("com.webcommander.AppResourceTagLib")
    AppResourceTagLib parent

    public static final RESOURCES_PATH = [:]
}

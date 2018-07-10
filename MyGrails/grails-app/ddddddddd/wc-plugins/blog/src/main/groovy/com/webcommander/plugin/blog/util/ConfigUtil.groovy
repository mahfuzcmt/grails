package com.webcommander.plugin.blog.util

class ConfigUtil {
    static POST_IMAGE_SETTINGS = [
        LISTVIEW: "listview",
        CATEGORY_DETAILS: "cat_details"
    ]

    static setPostImageConfig(Map site_config) {
        def configMap = [:]
        POST_IMAGE_SETTINGS.each { key, value ->
            configMap[value] = "" + getPostImageSize(Math.max(site_config[value + '_width'].toInteger(), site_config[value + '_height'].toInteger()))
        }
        site_config.size = configMap
    }

    private static int getPostImageSize(Integer size) {
        if(size <= 600) {
            return Math.ceil(size / 150) * 150
        }
        return 900
    }
}
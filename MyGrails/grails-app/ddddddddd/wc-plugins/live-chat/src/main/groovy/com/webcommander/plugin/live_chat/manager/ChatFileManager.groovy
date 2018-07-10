package com.webcommander.plugin.live_chat.manager

import com.webcommander.util.StringUtil
import org.springframework.web.multipart.MultipartFile

import java.util.concurrent.ConcurrentHashMap

class ChatFileManager {
    private static ConcurrentHashMap<String, Map> FILE_HOLDER = new ConcurrentHashMap();

    static void pushFile(String key, String filePath, String chatId) {
        Map map = [
            chatId: chatId,
            filePath: filePath
        ];
        FILE_HOLDER.put(key, map);
    }

    static String pushFile(String filePath, String chatId) {
        String key = StringUtil.uuid;
        pushFile(key, filePath, chatId);
        return key;
    }

    static Map getFile(String key) {
        return FILE_HOLDER[key];
    }

    static Boolean removeFile(String key) {
        return FILE_HOLDER.remove(key);
    }
}

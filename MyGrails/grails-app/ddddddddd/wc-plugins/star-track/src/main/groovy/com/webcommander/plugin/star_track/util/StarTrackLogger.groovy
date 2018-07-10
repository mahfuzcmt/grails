package com.webcommander.plugin.star_track.util

import grails.util.Holders
import groovy.util.logging.Log

import java.util.logging.Level

/**
 * Created by sajedur on 6/24/2015.
 */
@Log
class StarTrackLogger {

    public static synchronized void logText(String xml) {
        try {
            String parentPath = Holders.servletContext.getRealPath("WEB-INF/system-resources/log")
            File parent = new File(parentPath)
            if(!parent.exists()) {
                parent.mkdirs()
            }
            File file = new File(parentPath + "/star-track.log")
            FileWriter fileWriter = new FileWriter(file, true)
            fileWriter.write("\r\n\r\n" + new Date().toString() + "\r\n\r\n");
            fileWriter.write(xml)
            fileWriter.close()
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.message, ex)
        }
    }

    public static void requestXml(String requestXML) {
        requestXML = "Request XML \r\n ----------------------------------- \r\n\r\n" + requestXML + "\r\n\r\n"
        logText(requestXML)
    }

    public static void responseXml(String responseXMl) {
        responseXMl = "Response XML \r\n ----------------------------------- \r\n\r\n" + responseXMl + "\r\n\r\n"
        logText(responseXMl)
    }
}

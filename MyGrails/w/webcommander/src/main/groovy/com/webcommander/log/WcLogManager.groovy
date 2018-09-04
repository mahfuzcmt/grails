package com.webcommander.log

import grails.util.Holders
import org.apache.log4j.Appender
import org.apache.log4j.DailyRollingFileAppender
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.spi.LoggingEvent

class WcLogManager {
    private static Logger _logger
    private static DailyRollingFileAppender _appender

    static Logger getLogger() {
        if(_logger) {
            return _logger
        }
        _logger = LogManager.getLogger(WcLogManager.class)
        _logger.setLevel(Level.TRACE)
        return _logger
    }

    static Appender getAppender() {
        if(_appender) {
            return _appender
        }
        PatternLayout layout = new PatternLayout("%d %-5p %c %x - %m%n")
        String logsFolderPath = Holders.servletContext.getRealPath("WEB-INF/logs")
        File logsFolder = new File(logsFolderPath)
        if(!logsFolder.exists()) {
            logsFolder.mkdir()
        }
        _appender = new WcDailyRollingFileAppender(layout, logsFolderPath + File.separator + "wc-logs.log", "'.'yyyy-MM-dd")
        return _appender
    }

    static void log(String message, String loggerName) {
        try {
            LoggingEvent event = new WcLoggingEvent("WcLogManager", logger, Level.TRACE, message, null)
            event.setLoggerName(loggerName)
            appender.doAppend(event)
        } catch (Throwable ignored) {
            logger.error("Can't log message: ${message} \nCause: ${ignored.message}")
        }
    }

    static void log(String message, Map conf) {
        Map replaces = conf.replaces ?: [:]
        replaces.each { String key, String value->
            message = message.replace(key, value)
        }
        log(message, conf.loggerName ?: "WcCommonLogger")
    }

    static void consoleLog(String message, String loggerName) {
        PatternLayout layout = new PatternLayout("%d %-5p %c %x - %m%n")
        LoggingEvent event = new WcLoggingEvent("WcLogManager", logger, Level.TRACE, message, null)
        event.setLoggerName(loggerName)
        log(layout.format(event), [:])
    }
}

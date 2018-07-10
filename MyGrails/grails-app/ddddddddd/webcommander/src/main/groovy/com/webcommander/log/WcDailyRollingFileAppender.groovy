package com.webcommander.log

import org.apache.log4j.DailyRollingFileAppender
import org.apache.log4j.Layout
import org.apache.log4j.spi.LoggingEvent

class WcDailyRollingFileAppender extends DailyRollingFileAppender {

    public WcDailyRollingFileAppender (Layout layout, String filename, String datePattern) throws IOException {
        super(layout, filename, datePattern)
    }

    public void subAppend(LoggingEvent event) {
        super.subAppend(event)
    }
}

package com.webcommander.plugin.visitor_listing


import java.util.concurrent.TimeUnit

class VisitorListingTagLib {
    static namespace = "visitor"

    def toDuration = { attr, body ->
        Long millis = attr.millis
        long SECOND = 1000;
        long MINUTE = 60 * SECOND
        out << TimeUnit.MILLISECONDS.toMinutes(millis) + "m "
        millis = millis % MINUTE
        out << TimeUnit.MILLISECONDS.toSeconds(millis) + "s"
    }
}

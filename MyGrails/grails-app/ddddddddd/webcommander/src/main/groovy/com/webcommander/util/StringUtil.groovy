package com.webcommander.util

import grails.web.servlet.mvc.GrailsParameterMap
import grails.converters.JSON

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by zobair on 19/11/13.*/
class StringUtil {
    static String getCapitalizedAndPluralName(String name) {
        String pname = name.capitalize() + "s"
        if(pname.endsWith("ys")) {
            pname = pname.replaceAll(/ys$/, "ies")
        } else if(pname.endsWith("xs")) {
            pname = pname.replaceAll(/xs$/, "xes")
        }
        return pname
    }

    static String getUuid() {
        return UUID.randomUUID().toString().toUpperCase()
    }

    static String RemoveHTMLTag(String html) {
        if (html) {
            return html.replaceAll("</?[^>]*>", "")
        } else {
            return ""
        }
    }

    static Map extractPercentNumber(String number) {
        Pattern x = ~/([0-9\.]+)(%)?/
        Matcher matcher =  (number =~ x)
        if(matcher.find()) {
            return [number: matcher.group(1), sign: matcher.group(2)]
        }
        return [number: null, sign: null]
    }

    static def autoCast(String value) {
        if (value == "{}") {
            return [:]
        }
        if (value == "[]") {
            return []
        }
        if (value =~ /^\[("[^"]+"[^=:]|\d+)/) {
            return JSON.parse(value)
        }
        if (value =~ /^(\{|\[\{)"[^"]+":/) {
            return JSON.parse(value)
        }
        if (value =~ /^\s*-?\d+\s*$/) {
            return value.toLong()
        }
        if (value =~ /^\s*-?(\d+)?\.\d+\s*$/) {
            return value.toDouble()
        }
        if (value =~ /^\s*(true|yes|on)\s*$/) {
            return true
        }
        if (value =~ /^\s*(false|no|off)\s*$/) {
            return false
        }
        return value
    }

    static GrailsParameterMap convertToNestedKeyMap(Map map) {
        Map newMAp = new GrailsParameterMap([:], null)
        if(map) {
            newMAp.updateNestedKeys(map)
        }
        return newMAp
    }
}
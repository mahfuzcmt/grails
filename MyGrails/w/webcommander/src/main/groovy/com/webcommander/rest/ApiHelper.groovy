package com.webcommander.rest

import com.webcommander.constants.DomainConstants
import com.webcommander.log.WcLogManager
import com.webcommander.util.AppUtil
import groovy.json.JsonOutput
import groovy.util.slurpersupport.Node
import org.grails.web.json.JSONArray
import grails.web.servlet.mvc.GrailsParameterMap
import javax.servlet.http.HttpServletRequest

/**
 * Created by sajedur on 23-02-2015.
 */
class ApiHelper {
    public static final String FORMAT_XML = "xml"
    public static final String FORMAT_JSON = "json"
    public static final List CONTENT_TYPE_XML = ["application/xml", 'text/xml', 'text/xml; charset=utf-8']
    public static final List CONTENT_TYPE_JSON = ["application/json", 'text/json', 'text/json; charset=utf-8']

    private static def mapFromJSON(def object) {
        if (object == null || object instanceof String || object instanceof Number || object instanceof Boolean || object instanceof Date) {
            return object
        }
        if(object == null) {
            return null
        }
        if (object instanceof JSONArray) {
            return object.collect {
                mapFromJSON(it)
            }
        }
        return object.collectEntries {
            [(it.key): mapFromJSON(it.value)]
        }
    }

    private static def mapFromXML(def object) {
        if (object instanceof String) {
            return object
        } else if (object instanceof Node) {
            if (object.children().size() == 1) {
                return [(object.name()): mapFromXML(object.children().first())]
            }
            if (object.children().size() > 1) {
                def map = [:]
                object.children().each {
                    def value = mapFromXML(it)
                    String key = value.keySet().first()
                    if (map.containsKey(key)) {
                        if (map[key] instanceof List) {
                            map[key].add(value.get(key))
                        } else {
                            map[key] = [map[key], value.get(key)]
                        }
                    } else {
                        map << value
                    }
                }
                return [(object.name()): map]
            }

            return [(object.name()): null]
        } else {
            def result = mapFromXML(object.getAt(0))
            return result.get(result.keySet().first())
        }
    }

    private static def mapFromRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if(contentType in CONTENT_TYPE_JSON && request.JSON) {
            return mapFromJSON(request.JSON)
        } else if(contentType in CONTENT_TYPE_XML && request.XML) {
            return  mapFromXML(request.XML)
        }
        return [:]
    }

    static def setRequestParams(HttpServletRequest request, GrailsParameterMap params) {
        Map requestBody = mapFromRequest(request);
        params.putAll(requestBody)
        request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY, requestBody)
        request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.IS_API_REQUEST, true)
    }

    static def logRequest() {
        def request = AppUtil.request, params = AppUtil.params
        StringBuilder builder = new StringBuilder();
        builder.append("\nHeaders:\n")
        request.getHeaderNames().each {
            builder.append(it + ": " + request.getHeader(it))
            builder.append("\n")
        }
        builder.append("\nParams:")
        builder.append(JsonOutput.prettyPrint(JsonOutput.toJson(params)))
        WcLogManager.consoleLog(builder.toString(), "APIDebugLog")
    }
}
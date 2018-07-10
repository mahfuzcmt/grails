package com.webcommander.helper

import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by sajedur on 05-03-2015.
 */
class ApiUrlMappingHelper {
    static Map mappings = [
        "admin#zone#list" : [
            action: "zoneList",
            controller: "apiAppAdmin"
        ]
    ]


    static String getController(GrailsParameterMap params, Boolean isAdmin = false) {
        String key = (isAdmin ? "admin#" : "") + params._controller + (params._child ? "#" + params._child : "") + "#" + params._action;
        return mappings[key] ? mappings[key].controller : "api" + params._controller.camelCase() + (isAdmin ? "Admin" : "")
    }

    static String getAction(GrailsParameterMap params, Boolean isAdmin = false) {
        String key = (isAdmin ? "admin#" : "") + params._controller + (params._child ? "#" + params._child : "") + "#" + params._action;
        return mappings[key] ? mappings[key].action : ((params._child ?  params._child + "-" : "") + params._action).camelCase(false)
    }
}

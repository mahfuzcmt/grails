package com.webcommander.license.blocker

import com.webcommander.Page
import com.webcommander.models.License
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by zobair on 08/02/2015.
 */
class PageLicense {
    public static class Edit {
        public static isApplicable(String license, GrailsParameterMap params) {
            if(!params.id) {
                return true
            }
        }

        public static int limitCheck(License license, GrailsParameterMap params) {
            PageLicense.limitCheck(license, 1)
        }
    }

    public static class Save {
        public static isApplicable(String license, GrailsParameterMap params) {
            if(!params.id) {
                return true
            } else if(params.active.toBoolean(true)) {
                Page page = Page.findById(params.long("id") ?: 0)
                if(page && !page.isActive) {
                    return true
                }
            }
        }

        public static int limitCheck(License license, GrailsParameterMap params) {
            PageLicense.limitCheck(license, 1)
        }
    }

    public static class Copy {
        public static isApplicable(String license, GrailsParameterMap params) {
            if(params.id) {
                return Page.findById(params.long("id") ?: 0).isActive == 1
            } else {
                Page.findAllByIdInListAndIsActive(params.list("ids").collect {it.toLong(0)}, true).size() > 0
            }
        }

        public static int limitCheck(License license, GrailsParameterMap params) {
            PageLicense.limitCheck(license, params.id ? 1 : Page.findAllByIdInListAndIsActive(params.list("ids").collect {it.toLong(0)}, true).size())
        }
    }

    private static int limitCheck(License license, int addableCount) {
        int currentCount = Page.countByIsActive(true) + addableCount
        if(!license.limit || currentCount <= license.limit) {
            return 0
        }
        if(license.isLimitExtensible) {
            return 2
        }
        return 1
    }
}
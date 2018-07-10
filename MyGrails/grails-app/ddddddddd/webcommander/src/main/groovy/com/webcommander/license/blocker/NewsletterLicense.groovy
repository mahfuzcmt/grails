package com.webcommander.license.blocker

import com.webcommander.models.License
import com.webcommander.webmarketting.Newsletter
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by zobair on 09/02/2015.
 */
class NewsletterLicense {
    public static isApplicable(String license, GrailsParameterMap params) {
        if(!params.id) {
            return true
        } else if(params.active.toBoolean(true)) {
            Newsletter newsletter = Newsletter.findById(params.long("id") ?: 0)
            if(newsletter && !newsletter.isActive) {
                return true
            }
        }
    }

    public static int limitCheck(License license, GrailsParameterMap params) {
        int currentCount = Newsletter.countByIsActive(true)
        if(!license.limit || currentCount < license.limit) {
            return 0
        }
        if(license.isLimitExtensible) {
            return 2
        }
        return 1
    }
}
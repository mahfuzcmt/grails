package com.webcommander.license.blocker

import com.webcommander.admin.Operator
import com.webcommander.license.validator.OperatorValidator
import com.webcommander.models.License
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Created by zobair on 09/02/2015.
 */
class OperatorLicense {

    public static isApplicable(String license, GrailsParameterMap params) {
        if(!params.id) {
            return true
        } else if(params.active.toBoolean(true)) {
            Operator operator = Operator.findById(params.long("id") ?: 0)
            if(operator && !operator.isActive) {
                return true
            }
        }
    }

    public static int limitCheck(License license, GrailsParameterMap params) {
        int currentCount = OperatorValidator.currentCount
        if(!license.limit || currentCount < license.limit) {
            return 0
        }
        if(license.isLimitExtensible) {
            return 2
        }
        return 1
    }
}
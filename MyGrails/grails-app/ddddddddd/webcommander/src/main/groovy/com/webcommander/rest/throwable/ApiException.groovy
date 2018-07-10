package com.webcommander.rest.throwable

import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * Created by sajedur on 19-02-2015.
 */
class ApiException extends RuntimeException {
    private String message
    private Integer statusCode
    private List args
    private static ApplicationTagLib _g
    private static ApplicationTagLib getG() {
        _g ?: (_g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }
    public ApiException(String message) {
        this.message = message
    }

    public ApiException(String message, Integer statusCode) {
        this.message = message
        this.statusCode = statusCode
    }

    public ApiException(String message, Integer statusCode, List args) {
        this(message, statusCode)
        this.args = args
    }

    public ApiException(String message, List args) {
        this(message)
        this.args = args
    }

    public String getMessage() {
        g.message(code: message, args: args)
    }

    public Integer getStatusCode() {
        return statusCode
    }
}

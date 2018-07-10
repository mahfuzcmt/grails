package com.webcommander.throwables

import com.webcommander.LocalizationTagLib
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib

import javax.servlet.http.HttpServletRequest

/**
 * Created by zobair on 03/06/13.
 */
class ApplicationRuntimeException extends RuntimeException {

    protected String message
    List args = []
    protected List raws = []

    private Integer errorCode

    ApplicationRuntimeException() {}

    /**
     * @param message must be a valid code listed in our message.properties file
     */
    ApplicationRuntimeException(String message, Integer errorCode = null) {
        this.@message = message
        this.@errorCode = errorCode
    }

    ApplicationRuntimeException(String message, List args, Integer errorCode = null) {
        this.@message = message
        this.@args = args
        this.@errorCode = errorCode
    }

    ApplicationRuntimeException(String message, List args, List raws, Integer errorCode = null) {
        this.@message = message
        this.@args = args ?: []
        this.@raws = raws ?: []
        this.@errorCode = errorCode
    }

    ApplicationRuntimeException(String message, Throwable parentCause) {
        super(parentCause.message, parentCause)
        this.@message = message
    }

    ApplicationRuntimeException(String message, List args, Throwable parentCause) {
        super(parentCause.message, parentCause)
        this.@message = message
        this.@args = args
    }

    ApplicationRuntimeException(String message, List args, List raws, Throwable parentCause) {
        super(parentCause.message, parentCause)
        this.@message = message
        this.@args = args ?: []
        this.@raws = raws ?: []
    }

    String getLocalizedMessage() {
        Map rawArgs = [:]
        int rawHandled = 0
        List removAbles = []
        args.eachWithIndex { rule, i ->
            if (rule == null) {
                rawArgs[rawHandled++] = raws.remove(0)
                removAbles << i
            }
        }
        removAbles.each {
            args.remove it
        }
        raws.each {
            rawArgs[rawHandled++] = it
        }
        def doTheTrick = {
            AppUtil.getBean(LocalizationTagLib).message(code: this.@message, args: args, rawargs: rawArgs)
        }
        if (HttpServletRequest.isBound()) {
            doTheTrick()
        } else {
            HttpServletRequest.mock doTheTrick
        }
    }

    String getMessage() {
        return this.@message
    }

    Integer getErrorCode() {
        return this.errorCode
    }
}

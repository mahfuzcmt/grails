package com.webcommander.throwables.sso

import grails.util.Holders
import org.apache.commons.httpclient.HttpStatus
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * Created by sajedur on 08-02-2015.
 */
class OAuthException extends RuntimeException {
    private String msgCode
    private Integer statusCode = HttpStatus.SC_BAD_REQUEST
    private String errorCode
    private List messageParams = []

    private static ApplicationTagLib _g
    private static ApplicationTagLib getG() {
        _g ?: (_g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib))
    }
    public OAuthException(String msgCode){
        this.msgCode = msgCode
    }

    public OAuthException(String msgCode, List args, String errorCode, Integer statusCode) {
        this(msgCode, args, statusCode)
        this.errorCode = errorCode
    }

    public OAuthException(String msgCode, List args){
        this.msgCode = msgCode
        this.messageParams = args
    }

    public OAuthException(String msgCode, String errorCode){
        this(msgCode)
        this.errorCode = errorCode
    }

    public OAuthException(String msgCode, String errorCode, Integer statusCode) {
        this(msgCode, errorCode)
        this.statusCode = statusCode
    }

    public OAuthException(String msgCode, int statusCode){
        this(msgCode)
        this.statusCode = statusCode
    }

    public OAuthException(String msgCode, List args, int statusCode){
        this(msgCode, args)
        this.statusCode = statusCode
    }

    public String getMsgCode(){
        return msgCode
    }

    public int getStatusCode() {
        return statusCode
    }

    public String getErrorCode() {
        return errorCode
    }

    public List getMessageParams() {
        return messageParams;
    }

    public String getMessage() {
//        g.message(code: message, args: messageParams)
        return this.msgCode
    }
}

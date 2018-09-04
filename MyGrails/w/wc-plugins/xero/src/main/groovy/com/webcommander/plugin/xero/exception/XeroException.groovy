package com.webcommander.plugin.xero.exception

import com.webcommander.throwables.ApplicationRuntimeException
import grails.util.Holders
import org.springframework.context.MessageSource

/**
 * Created by sajed on 6/12/2014.
 */
class XeroException extends ApplicationRuntimeException {

    private static MessageSource messageSource
    private static MessageSource getMessageSource() {
        return  messageSource ?: (messageSource = Holders.grailsApplication.mainContext.getBean("messageSource"))
    }

    String messageCode;
    List messageArgs;

    public XeroException(String messageCode){
        this(messageCode, [])
    }

    public XeroException(String messageCode, List params){
        this.messageCode = messageCode
        this.messageArgs = params
    }

    public XeroException(String messageCode, Throwable cause){
        super(messageCode, cause)
    }

    public XeroException(String messageCode, List params, Throwable cause){
        super(messageCode, params, cause)
    }

    public String getMessage() {
        getMessageSource().getMessage(messageCode, messageArgs as Object[], messageCode, Locale.default)
    }
}

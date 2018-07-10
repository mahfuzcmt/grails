package com.webcommander.plugin.myob.exceptions

import com.webcommander.throwables.ApplicationRuntimeException
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.MessageSource

/**
 * Created by sanjoy on 3/3/14.
 */
class MyOBException extends ApplicationRuntimeException{
    private static MessageSource messageSource
    private static MessageSource getMessageSource() {
       return  messageSource ?: (messageSource = Holders.grailsApplication.mainContext.getBean("messageSource"))
    }

    String messageCode;
    List messageArgs;


    public MyOBException(String messageCode){
        this(messageCode, [])
    }

    public MyOBException(String messageCode, List params){
        this.messageCode = messageCode
        this.messageArgs = params
    }

    public MyOBException(String messageCode, Throwable cause){
        super(messageCode, cause)
    }

    public MyOBException(String messageCode, List params, Throwable cause){
        super(messageCode, params, cause)
    }

    public String getMessage() {
        getMessageSource().getMessage(messageCode, messageArgs as Object[], messageCode, Locale.default)
    }
}

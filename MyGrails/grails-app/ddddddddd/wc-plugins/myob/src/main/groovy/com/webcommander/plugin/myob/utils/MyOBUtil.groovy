package com.webcommander.plugin.myob.utils

import grails.web.context.ServletContextHolder
import org.grails.web.util.GrailsApplicationAttributes


/**
 * Created by sanjoy on 3/5/14.
 */
class MYOBUtil {

    public static def getServiceByName(String name){
        String serviceName = name.endsWith("Service") ? name : "${name}Service";
        def ctx = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
        return ctx.getBean(serviceName);
    }
}

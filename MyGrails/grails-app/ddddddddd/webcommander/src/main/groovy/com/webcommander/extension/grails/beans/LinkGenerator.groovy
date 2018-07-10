package com.webcommander.extension.grails.beans

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import org.grails.web.mapping.DefaultLinkGenerator

import javax.servlet.http.HttpServletRequest

/**
 * Created by LocalZobair on 16/03/2017.*/
class LinkGenerator extends DefaultLinkGenerator implements grails.web.mapping.LinkGenerator {
    LinkGenerator(String serverBaseURL, String contextPath) {
        super(serverBaseURL, contextPath)
    }

    LinkGenerator(String serverBaseURL) {
        super(serverBaseURL)
    }

    String makeServerURL() {
        HttpServletRequest request = AppUtil.request
        String scheme = request && !request.IS_DUMMY ? request.scheme : "http"
        return AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, scheme == "http" ? "baseurl" : "secured_baseurl")
    }
}
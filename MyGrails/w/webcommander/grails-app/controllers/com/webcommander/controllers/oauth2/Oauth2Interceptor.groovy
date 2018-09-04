package com.webcommander.controllers.oauth2

import com.webcommander.constants.DomainConstants
import com.webcommander.oauth2.OAuthClient
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil


class Oauth2Interceptor {

    static excludeInterceptor = ["mobileAuth"]

    boolean before() {
        def controller = AppUtil.getBean(controllerClass.clazz)
        controller.credentials = request.getAttribute(DomainConstants.REQUEST_ATTR_KEYS.REQUEST_BODY)
        controller.credentials = controller.credentials ?: params
        if(!(actionName in excludeInterceptor) ) {
            controller.client = OAuthClient.createCriteria().get {
                eq("clientId", controller.credentials[DomainConstants.OAUTH_CONSTANTS.CLIENT_ID])
            }
            if (!controller.client) {
                throw new OAuthException("no.client.found", DomainConstants.OAUTH_ERROR_CODE.INVALID_CLIENT)
            }
            if(!controller.client.enabled) {
                throw new OAuthException("application.disabled", DomainConstants.OAUTH_ERROR_CODE.INVALID_CLIENT)
            }
            if(controller.credentials[DomainConstants.OAUTH_CONSTANTS.REDIRECT_URI] != controller.client.redirectUrl) {
                throw new OAuthException("invalid.redirect.url", DomainConstants.OAUTH_ERROR_CODE.INVALID_REDIRET_URI)
            }
        }
        return true
    }

    boolean after() { true }

    void afterView() { true }
}

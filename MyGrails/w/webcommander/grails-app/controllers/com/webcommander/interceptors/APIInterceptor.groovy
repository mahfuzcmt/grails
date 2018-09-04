package com.webcommander.interceptors

import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.license.validator.ApiValidator
import com.webcommander.manager.LicenseManager
import com.webcommander.models.License
import com.webcommander.oauth.OauthProviderService
import com.webcommander.oauth2.OAuthAccess
import com.webcommander.rest.ApiHelper
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import org.apache.commons.httpclient.HttpStatus

class APIInterceptor extends RestProcessor {
    OauthProviderService oauthProviderService

    APIInterceptor() {
        match(controller: ~/^api.*/)
        match(controller: ~/^OAuth2.*/)
    }

    boolean before() {
        ApiHelper.setRequestParams(request, params)
        if(params.debug) ApiHelper.logRequest()
        try {
            String accessToken = params[DomainConstants.OAUTH_CONSTANTS.ACCESS_TOKEN] ?: request.getHeader(DomainConstants.OAUTH_CONSTANTS.ACCESS_TOKEN)
            if(!accessToken) {
                throw new OAuthException("invalid.api.request", DomainConstants.OAUTH_ERROR_CODE.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST)
            }
            OAuthAccess access = oauthProviderService.getOAuthAccess(accessToken);
            if(!access) {
                throw new OAuthException("invalid.access.token", DomainConstants.OAUTH_ERROR_CODE.INVALID_ACEESS_TOKEN, HttpStatus.SC_UNAUTHORIZED)
            }
            if(!access.client.enabled) {
                throw new OAuthException("client.disabled", DomainConstants.OAUTH_ERROR_CODE.DISABLE_CLIENT, HttpStatus.SC_UNAUTHORIZED)
            }
            if((new Date().gmt().time - access.updated.time) > DomainConstants.OAUTH_CONSTANTS.EXPIRE_TIME * 1000) {
                throw new OAuthException("token.expire", DomainConstants.OAUTH_ERROR_CODE.ACCESS_TOKEN_EXPIRE, HttpStatus.SC_UNAUTHORIZED)
            }
            request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.API_CLIENT, access.client.id)
            request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.ADMIN, access.operator?.id)
            request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.CUSTOMER, access.customer?.id)
            if (LicenseManager.isProvisionActive()) {
                Long currentTime = System.currentTimeMillis()
                if(currentTime > ApiValidator.nextUpdateDate.getTime()) {
                    ApiValidator.updateNextUpdateDate()
                }
                License license = LicenseManager.license(NamedConstants.LICENSE_KEYS.API)
                if(license) {
                    if(license.limit && AppUtil.api_monthly_hit_count >= license.limit) {
                        throw new OAuthException("limit.api.exceeded.limit", [license.limit], "api_request_limit_exceeded", HttpStatus.SC_FORBIDDEN)
                    }
                } else {
                    throw new OAuthException("feature.disabled", "api_feature_disabled", HttpStatus.SC_FORBIDDEN)
                }
                AppUtil.api_monthly_hit_count++
            }
        } catch (OAuthException ex) {
            modelAndView = null
            response.status = ex.statusCode
            rest status: "error", message: ex.message, code: ex.errorCode
            return false
        }
        return true
    }

    boolean after() { true }

    void afterView() { true }
}

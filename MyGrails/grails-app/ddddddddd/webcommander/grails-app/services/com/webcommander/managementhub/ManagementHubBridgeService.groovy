package com.webcommander.managementhub

import com.webcommander.admin.ConfigService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CacheManager
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpDeleteMethodUtil
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity


class ManagementHubBridgeService {

    private static final Integer HTTP_STATUS_SUCCESS = 200
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json"
    private static final String UTF_8_CHARSET = "UTF-8"
    private static final String RENEW_ACCESS_TOKEN = "manage-base/api/v1/write/auth-client/token"
    ConfigService configService

    String getProvisionServerUrl() {
        String cacheWebCommanderUrl = AppUtil.getProvisioningApiCredential().url
        if (!cacheWebCommanderUrl) {
            throw new OAuthException("license.url.not.found")
        }
        if(cacheWebCommanderUrl.endsWith("/")) {
            AppUtil.getProvisioningApiCredential().url = cacheWebCommanderUrl = cacheWebCommanderUrl.substring(0, cacheWebCommanderUrl.length() - 1)
        }
        return cacheWebCommanderUrl
    }

    def sentAndParseDeleteRequest(String url, Map params){
        url = "${provisionServerUrl}/${url}"
        return sentDeleteRequest(url, params)
    }

    def sentDeleteRequest(String url, Map params){
        HttpMethod httpMethod = new HttpDeleteMethodUtil(url)
        httpMethod.setRequestEntity(getJsonPostParams(params))
        httpMethod.setRequestHeader("Content-Type", CONTENT_TYPE_APPLICATION_JSON)
        return parseJson(httpMethod)
    }

    def parseJson( HttpMethod httpMethod ){
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    String getAccessToken() {
        String cacheWebCommanderAccessToken = AppUtil.getProvisioningApiCredential().accessToken
        if (!cacheWebCommanderAccessToken) {
            throw new OAuthException("access.token.not.found")
        }
        return cacheWebCommanderAccessToken
    }

    private String getClientId() {
        String cacheWebCommanderClientId = AppUtil.getProvisioningApiCredential().clientId
        if (!cacheWebCommanderClientId) {
            throw new OAuthException("client.id.not.found")
        }
        return cacheWebCommanderClientId
    }

    private String getClientSecret() {
        String cacheWebCommanderClientSecret = AppUtil.getProvisioningApiCredential().clientSecret
        if (!cacheWebCommanderClientSecret)
            throw new OAuthException("client.secret.not.found")
        return cacheWebCommanderClientSecret
    }

    private String getAuthorizationCode() {
        String cacheWebCommanderCode = AppUtil.getProvisioningApiCredential().code
        if (!cacheWebCommanderCode) {
            throw new OAuthException("token.code.not.found")
        }
        return cacheWebCommanderCode
    }

    private String getRefreshToken() {
        String cacheWebCommanderRefreshToken = AppUtil.getProvisioningApiCredential().refreshToken
        if (!cacheWebCommanderRefreshToken) {
            throw new OAuthException("refresh.token.not.found")
        }
        return cacheWebCommanderRefreshToken
    }

    private StringRequestEntity getJsonPostParams(Map params) {
        String jsonString = params as JSON
        return new StringRequestEntity(jsonString, CONTENT_TYPE_APPLICATION_JSON, UTF_8_CHARSET)
    }

    String getRedirectUri() {
        //TODO: have to get appropriate server host
        return "http://" + "" + "/wizard/apiCallback"
    }

    private def retrieveTokens(Integer statusCode) {
        String url = provisionServerUrl + "/${RENEW_ACCESS_TOKEN}"
        HttpMethod httpMethod = new PostMethod(url)
        def params = [:]
        params.put("client_id", getClientId())
        params.put("client_secret", getClientSecret())
        httpMethod.setRequestHeader("Content-Type", CONTENT_TYPE_APPLICATION_JSON)
        if (statusCode == 1401) {
            params.put("grant_type", "refresh_token")
            params.put("refresh_token", getRefreshToken())
        } else {
            params.put("grant_type", "authorization_code")
            params.put("code", getAuthorizationCode())
        }
        params.put("redirect_uri", getRedirectUri())
        httpMethod.setRequestEntity(getJsonPostParams(params))
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            def result = slurper.parseText(method.getResponseBodyAsString())
            Map provision = AppUtil.getProvisioningApiCredential()
            result = result.responseData
            provision.refreshToken = result.refresh_token
            provision.accessToken = result.access_token
            List licenseConfig = [
                    [
                            type     : DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION,
                            configKey: "provision",
                            value    : (provision as com.webcommander.converter.json.JSON).toString()
                    ]
            ]
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, provision, "provisioning_api_credential")
            configService.update(licenseConfig)
        }
    }

    private synchronized execute(HttpMethod httpMethod, Closure closure) {
        HttpClient httpClient = new HttpClient()
        httpMethod.addRequestHeader("access-token", getAccessToken())
        httpClient.executeMethod(httpMethod)
        if (httpMethod.statusCode == HTTP_STATUS_SUCCESS) {
            closure(httpMethod)
        } else {
            JsonSlurper slurper = new JsonSlurper()
            def result = slurper.parseText(httpMethod.getResponseBodyAsString())
            if ((result.statusCode == 1401 || result.statusCode == 1404)) {
                retrieveTokens(result.statusCode)
                httpMethod.removeRequestHeader("access-token")
                execute(httpMethod, { method ->
                    closure(method)
                })
            } else {
                throw new OAuthException(result.exception)
            }
        }
    }


    def listByPOST(String url, String offset = 0, String max = 20, Map filter = [:]) {
        url = "$provisionServerUrl/$url"
        HttpMethod httpMethod = new PostMethod(url)
        filter.put("offset", offset)
        filter.put("max", max)
        httpMethod.setRequestEntity(getJsonPostParams(filter))
        httpMethod.setRequestHeader("Content-Type", CONTENT_TYPE_APPLICATION_JSON)
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def sentAndParseListByGETRequest(String url, String offset = 0, String max = 20, String sort_order = null, String sort_filed = null){
        def response = listByGET(url, offset, max, sort_order, sort_filed)
        if (response.isSuccess){
            response = response.responseData
            response.isSuccess = true
        }
        return response
    }

    def sentAndParseFilteredListByGETRequest(String url, String offset = 0, String max = 20, String sort_order = null, String sort_filed = null){
        url = "$provisionServerUrl/$url&offset=${offset}&max=${max}&sort_order=${sort_order}&sort_filed=${sort_filed}"
        def response = filteredListByGET(url, offset, max, sort_order, sort_filed)
        if (response.isSuccess){
            response = response.responseData
            response.isSuccess = true
        }
        return response
    }

    def filteredListByGET(String url, String offset = 0, String max = 20, String sort_order = null, String sort_filed = null) {
        HttpMethod httpMethod = new GetMethod(url)
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def listByGET(String url, String offset = 0, String max = 20, String sort_order = null, String sort_filed = null) {
        url = "$provisionServerUrl/$url?offset=${offset}&max=${max}&sort_order=${sort_order}&sort_filed=${sort_filed}"
        return filteredListByGET(url, offset, max, sort_order, sort_filed)
    }

    def getByFieldValue(String url, String filed, String value) {
        url = "$provisionServerUrl/$url?filed=${filed}&value=${value}"
        HttpMethod httpMethod = new GetMethod(url)
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def sentAndParsePostRequest(String url, Map params){
        def response = sentPostRequest(url, params)
        if (response.isSuccess){
            response = response.responseData
            response.isSuccess = true
        }
        return response
    }

    def sentPostRequest(String url, Map params){
        url = "$provisionServerUrl/$url"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.setRequestEntity(getJsonPostParams(params))
        httpMethod.setRequestHeader("Content-Type", CONTENT_TYPE_APPLICATION_JSON)
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }


    def sentAndParseGetRequest(String url){
        def response = sentGetRequest(url)
        if (response.isSuccess){
            response = response.responseData
            response.isSuccess = true
        }
        return response
    }

    def sentGetRequest(String url){
        url = "$provisionServerUrl/$url"
        HttpMethod httpMethod = new GetMethod(url)
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

}

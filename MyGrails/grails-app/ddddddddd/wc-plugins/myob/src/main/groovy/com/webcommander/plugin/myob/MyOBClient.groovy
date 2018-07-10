package com.webcommander.plugin.myob

import com.webcommander.admin.ConfigService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.myob.constants.MYOB
import com.webcommander.plugin.myob.constants.OAuth2
import com.webcommander.plugin.myob.exceptions.MyOBException
import com.webcommander.plugin.myob.properties.MyOBProperties
import com.webcommander.plugin.myob.utils.MYOBUtil
import com.webcommander.util.AppUtil
import grails.converters.JSON
import org.apache.commons.codec.binary.Base64
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.*
import org.grails.web.json.JSONObject

/**
 * Created by sanjoy on 3/3/14.
 */
class MyOBClient {
    private static final String CONTENT_TYPE = "application/json; charset=iso-8859-1"
    private MyOBProperties myOBProperties;
    private String clientId, clientSecret, redirectUri, companyFileUsername, companyFilePassword, companyFileUri;
    private String code, cacheAccessToken, refreshToken;

    public MyOBClient(String clientId, String clientSecret, String redirectUri, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret
        this.redirectUri = redirectUri
        this.refreshToken = refreshToken
        myOBProperties = MyOBProperties.getInstance()
    }

    public void setClientProperties(String clientId, String clientSecret, String redirectUri, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.refreshToken = refreshToken
    }

    public void setCompanyFileProperties(String companyFileUri, String companyFileUsername, String companyFilePassword) {
        this.companyFileUsername = companyFileUsername;
        this.companyFilePassword = companyFilePassword;
        this.companyFileUri = companyFileUri;
    }

    public URL getAuthorizationUrl() {
        if ([clientId, clientSecret, redirectUri].any { it == null }) {
            throw new MyOBException("configuration.not.complete.msg")
        }
        String urlStr = "${myOBProperties.getAuthorizationUrl()}?${OAuth2.CLIENT_ID}=${clientId}&${OAuth2.SCOPE}=${OAuth2.COMPANY_FILE}&" +
                "${OAuth2.REDIRECT_URI}=${redirectUri}&${OAuth2.RESPONSE_TYPE}=${OAuth2.RESPONSE_TYPE_CODE}";
        return new URL(urlStr)
    }

    public boolean setCode(String code) {
        this.code = code;
        try {
            return accessToken != null
        } catch (MyOBException ex) {
            return false;
        }
    }

    private void setRefreshToken(String refreshToken){
        this.@refreshToken = refreshToken;
        List configs = [[type: DomainConstants.SITE_CONFIG_TYPES.MYOB, configKey: "refresh_token", value: refreshToken]]
        ConfigService configService = MYOBUtil.getServiceByName("config");
        configService.update(configs);

    }

    private def performRequest(HttpMethod method, int callCount = 0, boolean isTokenRequest = false) {
        if(!isTokenRequest && !cacheAccessToken){
            throw new MyOBException("access.token.not.found")
        }
        HttpClient client = new HttpClient()
        client.executeMethod(method)
        switch (method.statusCode) {
            case 200:
                try{
                    if(method.responseBodyAsString)
                        return JSON.parse(method.responseBodyAsStream, OAuth2.UTF_8)
                    else
                        return method.responseHeaders.find {it.name == "Location"}.value
                }catch (Exception exp){
                    exp.printStackTrace()
                    JSONObject resp = new JSONObject()
                    resp.statusCode= method.statusCode;
                    resp.statusText = method.statusText;
                    return resp;
                }
                break;
            case 201:
                try{
                    return method.responseHeaders.find {it.name == "Location"}.value
                }catch (Exception exp){
                    exp.printStackTrace()
                    JSONObject resp = new JSONObject()
                    resp.statusCode= method.statusCode;
                    resp.statusText = method.statusText;
                    return resp;
                }
                break;
            default:
                String response = method.responseBodyAsString
                JSONObject exception = JSON.parse(response);
                if (callCount < 2 && (exception.Message.toString().equalsIgnoreCase("access denied") || exception."Errors"?.collect{it.ErrorCode}.contains(31001))) {
                    cacheAccessToken = null
                    method.removeRequestHeader("Authorization")
                    method.addRequestHeader("Authorization", "Bearer ${accessToken}")
                    return performRequest(method, callCount + 1)
                }
                throw new MyOBException("myob.request.exception", [exception.toString()])
        }
    }

    private String getAccessToken() {
        if (cacheAccessToken) {
            return cacheAccessToken
        }
        PostMethod postMethod = new PostMethod(myOBProperties.getTokenUrl());
        if (refreshToken) {
            postMethod.addParameter(OAuth2.CLIENT_ID, clientId);
            postMethod.addParameter(OAuth2.CLIENT_SECRET, clientSecret);
            postMethod.addParameter(OAuth2.REFRESH_TOKEN, refreshToken);
            postMethod.addParameter(OAuth2.GRANT_TYPE, OAuth2.GRANT_TYPE_REFRESH)
        } else {
            if (!code) {
                throw new MyOBException("application.not.authorized")
            }
            postMethod.addParameter(OAuth2.CLIENT_ID, clientId);
            postMethod.addParameter(OAuth2.CLIENT_SECRET, clientSecret);
            postMethod.addParameter(OAuth2.REDIRECT_URI, redirectUri);
            postMethod.addParameter(OAuth2.CODE, code);
            postMethod.addParameter(OAuth2.SCOPE, OAuth2.COMPANY_FILE);
            postMethod.addParameter(OAuth2.GRANT_TYPE, OAuth2.GRANT_TYPE_ACCESS);
        }
        try {
            JSONObject response = performRequest(postMethod, 2, true)
            cacheAccessToken = null
            cacheAccessToken = response[OAuth2.ACCESS_TOKEN];
            setRefreshToken(response[OAuth2.REFRESH_TOKEN]);
            return cacheAccessToken
        } catch (MyOBException ex) {
            throw new MyOBException("could.not.retrieve.access.token", ex)
        }
    }

    public JSONObject performQuery(String resourceUrl, String query){
        String processedQuery = query.replaceAll(" ", "%20").replaceAll("'","%27")
        String queryUrl = resourceUrl + "/?" + processedQuery
        return performOperation(MYOB.READ, queryUrl);
    }

    public def performOperation(MYOB operation, String resourceUrl = null, String id = null, JSONObject object = null, String completeResourceUrl = null) {
        HttpMethod method;
        String requestUrl = completeResourceUrl ?: resourceUrl ? "${companyFileUri}/${resourceUrl}" : "${myOBProperties.getDatabaseUrl()}";
        if(id){
            requestUrl += "/${id}"
        }
        switch (operation) {
            case MYOB.READ:
                method = new GetMethod(requestUrl);
                break;
            case MYOB.CREATE:
                method = new PostMethod(requestUrl);
                RequestEntity requestEntity = new StringRequestEntity(object.toString(), CONTENT_TYPE, null);
                method.setRequestEntity(requestEntity);
                break;
            case MYOB.UPDATE:
                method = new PutMethod(requestUrl);
                RequestEntity requestEntity = new StringRequestEntity(object.toString(), CONTENT_TYPE, null);
                method.setRequestEntity(requestEntity)
                break;
            case MYOB.DELETE:
                method = new DeleteMethod(requestUrl);
                break
        }
        method.addRequestHeader("Authorization", "Bearer ${accessToken}");
        if (resourceUrl || completeResourceUrl) {
            method.addRequestHeader("x-myobapi-cftoken", new String(Base64.encodeBase64("${companyFileUsername}:${companyFilePassword}".bytes)));
        }
        method.addRequestHeader("x-myobapi-key", clientId);
        method.addRequestHeader("x-myobapi-version", "v2");
        def response =  performRequest(method);
        return response
    }

    public static String currentCompanyFileUid() {
        String companyFileUri = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MYOB, "company_file_uri");
        return companyFileUri?.substring(companyFileUri?.lastIndexOf('/') + 1)
    }
}

package com.webcommander.sso

import com.webcommander.admin.ConfigService
import com.webcommander.admin.Operator
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.converter.json.JSON
import com.webcommander.managementhub.ManagementHubBridgeService
import com.webcommander.manager.CacheManager
import com.webcommander.plugin.PluginManager
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

class ProvisionAPIService {
    ConfigService configService

    private static final Integer HTTP_STATUS_SUCCESS = 200
    private static final String ADD_ENTITY_URL = "manage-instance/api/v1/write/management-hub/entity-add"
    private static final String ENTITY_LIST_URL = "manage-instance/api/v1/read/management-hub/entity-list?identity_code="
    private static final String FETCH_LICENSE_URL = "manage-instance/api/v1/read/management-hub/fetch-license?identity_code="
    private static final String ENTITY_BY_TOKEN_URL = "manage-instance/api/v1/read/management-hub/entity-by-token?token="
    private static final String ENTITY_REMOVE_URL = "manage-instance/api/v1/write/management-hub/entity-remove"
    private static final String LOGIN_URL = "manage-instance/api/v1/read/management-hub/login-by-email-and-password"
    private static final String SUPER_ADMIN_BY_EIGHT_DIGIT_AND_PASSWORD_URL = "manage-instance/api/v1/read/management-hub/implementer-login"
    private static final String RENEW_ACCESS_TOKEN = "manage-base/api/v1/write/auth-client/token"

    ManagementHubBridgeService managementHubBridgeService

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

    String getAccessToken() {
        String cacheWebCommanderAccessToken = AppUtil.getProvisioningApiCredential().accessToken
        if (!cacheWebCommanderAccessToken) {
            throw new OAuthException("access.token.not.found")
        }
        return cacheWebCommanderAccessToken
    }

    String getWebCommanderLicenseCode() {
        String cacheWebCommanderLicense = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE, "licenseCode")
        if (!cacheWebCommanderLicense) {
            throw new OAuthException("license.code.not.found")
        }
        return cacheWebCommanderLicense
    }

    String getRedirectUri() {
        //TODO: have to get appropriate server host
        return "http://" + "" + "/wizard/apiCallback"
    }

    private HttpMethod addAccessToken(HttpMethod httpMethod) {
        httpMethod.addRequestHeader("access-token", getAccessToken())
        return httpMethod
    }

    private def retrieveTokens(String exception) {
        String url = provisionServerUrl + RENEW_ACCESS_TOKEN
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("client_id", getClientId())
        httpMethod.addParameter("client_secret", getClientSecret())
        if (exception.equals("Access Token Expired")) {
            httpMethod.addParameter("grant_type", "refresh_token")
            httpMethod.addParameter("refresh_token", getRefreshToken())
        } else {
            httpMethod.addParameter("grant_type", "authorization_code")
            httpMethod.addParameter("code", getAuthorizationCode())
        }
        httpMethod.addParameter("redirect_uri", getRedirectUri())
        execute(httpMethod) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            def result = slurper.parseText(method.getResponseBodyAsString())
            Map provision = AppUtil.getProvisioningApiCredential()
            provision.refreshToken = result.refresh_token
            provision.accessToken = result.access_token
            List licenseConfig = [
                    [
                            type     : DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION,
                            configKey: "provision",
                            value    : (provision as JSON).toString()
                    ]
            ]
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, provision, "provisioning_api_credential")
            configService.update(licenseConfig)
        }
    }

    private synchronized execute(HttpMethod httpMethod, Closure closure) {
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)
        if (httpMethod.statusCode == HTTP_STATUS_SUCCESS) {
            closure(httpMethod)
        } else {
            JsonSlurper slurper = new JsonSlurper()
            def result = slurper.parseText(httpMethod.getResponseBodyAsString())
            if ((result.exception.equals("Access Token Expired") || result.exception.equals("Invalid Access Token")) && !closure.class.name.startsWith(ProvisionAPIService.class.name + "\$_execute")) {
                retrieveTokens(result.exception)
                httpMethod.setRequestHeader("access-token", getAccessToken())
                execute(httpMethod, { method ->
                    closure method
                })
            } else {
                throw new OAuthException(result.exception)
            }
        }
    }

    def addEntity(String email, String uuid) {
        def requestMap = [:]
        requestMap.put("identity", getWebCommanderLicenseCode())
        requestMap.put("email", email)
        requestMap.put("password", "ChangeIt")
        requestMap.put("token", true)
        return managementHubBridgeService.sentAndParsePostRequest(ADD_ENTITY_URL, requestMap)
    }

    def removeEntity(String uuid) {
        def requestMap = [:]
        requestMap.put("identity_code", getWebCommanderLicenseCode())
        requestMap.put("entity_uuid", uuid)
        Map response = managementHubBridgeService.sentAndParseDeleteRequest(ENTITY_REMOVE_URL, requestMap)
        return throwExceptionIfFailedAndGetData(response)
    }

    private def throwExceptionIfFailedAndGetData(Map response){
        if (!response.isSuccess){
            return response
        } else {
            return response.responseData ?: response
        }
    }

    def entityList() {
        def operators = managementHubBridgeService.sentAndParseFilteredListByGETRequest(ENTITY_LIST_URL + getWebCommanderLicenseCode())
        return operators.list
    }

    def entityByToken(String token) {
        return managementHubBridgeService.sentAndParseGetRequest(ENTITY_BY_TOKEN_URL + token)
    }

    def entityByUuid(String uuid) {
        String url = "$provisionServerUrl/entity/entityByUuid?uuid=${uuid}&license=${getWebCommanderLicenseCode()}"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def approvalByUuid(String uuid) {
        String url = "$provisionServerUrl/entity/approvalByUuid?uuid=${uuid}&license=${getWebCommanderLicenseCode()}"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def implementerLogin(String eightDigit, String password) {
        def requestMap = [:]
        requestMap.put("eight_digit", eightDigit)
        requestMap.put("password", password)
        return managementHubBridgeService.sentAndParsePostRequest(SUPER_ADMIN_BY_EIGHT_DIGIT_AND_PASSWORD_URL, requestMap)
    }

    def entityLogin(String email, String password) {
        def requestMap = [:]
        requestMap.put("email", email)
        requestMap.put("password", password)
        return managementHubBridgeService.sentAndParsePostRequest(LOGIN_URL, requestMap)
    }

    def approveByToken(String token) {
        String url = provisionServerUrl + "/entity/approveByToken?token=${token}"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def emailByToken(String token) {
        String url = provisionServerUrl + "/entity/emailByToken?token=${token}&delete=false"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def changePassword(String email, String password) {
        String url = provisionServerUrl + "/entity/changePassword"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("email", email)
        httpMethod.addParameter("password", password)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def changePassword(String email, String password, String token) {
        String url = provisionServerUrl + "/entity/changePasswordByToken"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("email", email)
        httpMethod.addParameter("password", password)
        httpMethod.addParameter("token", token)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def changeEmail(String email, String newEmail) {
        String url = provisionServerUrl + "/entity/changeEmail"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("email", email)
        httpMethod.addParameter("newEmail", newEmail)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }


    def fetchLicense() {
        String licenseCode = getWebCommanderLicenseCode()
        return managementHubBridgeService.sentAndParseGetRequest(FETCH_LICENSE_URL + licenseCode)
    }

    def createToken(String email) {
        String url = "$provisionServerUrl/entity/createToken?license=${getWebCommanderLicenseCode()}&email=${email}"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def getAllPackageWithPlugin() {
        String url = "$provisionServerUrl/api_commander_provision/getAllPackageWithPlugin?license=${getWebCommanderLicenseCode()}"
        HttpMethod httpMethod = new GetMethod(url)

        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def getPluginsByLicense() {
        String url = "$provisionServerUrl/api_commander_provision/getPluginsByLicense?license=${getWebCommanderLicenseCode()}"
        HttpMethod httpMethod = new GetMethod(url)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def pluginConfirmation(String pluginName) {
        String url = "$provisionServerUrl/api_commander_provision/pluginConfirmation"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("pluginName", pluginName)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def pluginZip(String pluginName) {
        String url = "$provisionServerUrl/api_commander_provision/pluginZip"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("version", Holders.config.webcommander.version.number)
        httpMethod.addParameter("pluginName", pluginName)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            return method.getResponseBodyAsStream()
        }
    }

    def actionFeedBack(String status, String action) {
        String url = "$provisionServerUrl/api_commander_provision/actionFeedBack"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("status", status)
        httpMethod.addParameter("action", action)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def pluginInstalledUninstalled(String pluginName, String isInstalled) {
        String url = "$provisionServerUrl/api_commander_provision/pluginInstalledUninstalled"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("pluginName", pluginName)
        httpMethod.addParameter("isInstalled", isInstalled)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def upOrDownByOrderInvoicePayment(Map params) {
        String url = "$provisionServerUrl/api_my_account/upOrDownByOrderInvoicePayment"
        Map creditCard = [
                cardName  : params.cardName,
                cardNumber: params.cardNumber,
                cardCVV   : params.cardCVV,
                cardMonth : params.cardMonth,
                cardYear  : params.cardYear
        ]
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("cardDetails", (creditCard as JSON).toString())
        httpMethod.addParameter("to", params.packageId)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def accountDetailsByLicense() {
        String url = "$provisionServerUrl/api_my_account/accountDetailsByLicense"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def accountDetailsUpdateByLicense(Map params) {
        String url = "$provisionServerUrl/api_my_account/update"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        httpMethod.addParameter("displayName", params.displayName)
        httpMethod.addParameter("emailAddress", params.emailAddress)
        httpMethod.addParameter("mobile", params.mobile)
        httpMethod.addParameter("phone", params.phone)
        httpMethod.addParameter("addressLine1", params.addressLine1)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def getAliasByLicense() {
        String url = "$provisionServerUrl/api_commander_provision/getAliasByLicense"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def getAllAddon() {
        String url = "$provisionServerUrl/api_commander_provision/getAllAddon"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def getTemplateTypesAndCategories() {
        String url = "$provisionServerUrl/commander_provision/getTemplateTypesAndCategories"
        HttpMethod httpMethod = new PostMethod(url)
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def updateMyInstalledPlugin() {
//        String url = "$provisionServerUrl/api_commander_provision/updateMyInstalledPlugin"
//        HttpMethod httpMethod = new PostMethod(url)
//        httpMethod.addParameter("license", getWebCommanderLicenseCode())
//        String pluginList = (PluginManager.activePlugins.identifier as JSON).toString()
//        httpMethod.addParameter("pluginList", pluginList)
//        execute(addAccessToken(httpMethod)) { HttpMethod method ->
//            JsonSlurper slurper = new JsonSlurper()
//            return slurper.parseText(method.getResponseBodyAsString())
//        }
    }

    def commanderWebsitePackages() {
        String url = "$provisionServerUrl/api_commander_provision/commanderWebsitePackages"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    def restart() {
        String url = "$provisionServerUrl/api_commander_provision/restart"
        HttpMethod httpMethod = new PostMethod(url)
        httpMethod.addParameter("license", getWebCommanderLicenseCode())
        execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
    }

    List getAllInstances() {
        return []
        String url = "$provisionServerUrl/entity/instancesByEntityUuid?uuid=" + Operator.findById(AppUtil.session.admin).uuid
        HttpMethod httpMethod = new GetMethod(url)
        Map provisionResponse = execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
        provisionResponse.responseData.instances
    }

    Map getInstanceInfo(String instanceIdentity) {
        String url = "$provisionServerUrl/entity/getInstanceSwitchToken?instanceIdentity=$instanceIdentity&uuid=" + Operator.findById(AppUtil.session.admin).uuid
        HttpMethod httpMethod = new GetMethod(url)
        Map provisionResponse = execute(addAccessToken(httpMethod)) { HttpMethod method ->
            JsonSlurper slurper = new JsonSlurper()
            return slurper.parseText(method.getResponseBodyAsString())
        }
        provisionResponse.responseData
    }
}

package com.webcommander.common

import com.webcommander.constants.DomainConstants
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import com.webcommander.util.ConfigurationReader
import groovy.json.JsonSlurper
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part
import org.apache.commons.httpclient.methods.multipart.StringPart
import org.apache.commons.lang.math.RandomUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.multipart.MultipartFile

class CustomerPortalService {
    private String accessToken
    private String refreshToken
    ProvisionAPIService provisionAPIService

    private String getAccessToken() {
        if(accessToken) return accessToken
        throw new OAuthException("invalid.access.token", DomainConstants.OAUTH_ERROR_CODE.INVALID_ACEESS_TOKEN)
    }

    private String getRefreshToken() {
        if(refreshToken) return refreshToken
        throw new OAuthException("invalid.access.token", DomainConstants.OAUTH_ERROR_CODE.INVALID_REFRESH_TOKEN)
    }

    private String getEndPoint(String relativeUrl) {
        return ConfigurationReader.getProperty("customer_portal.server_url", "") + relativeUrl
    }

    private def retrieveTokens(String exception_code) {
        try {
            String url = getEndPoint("/oauth2/token")
            HttpMethod httpMethod = new PostMethod(url)
            httpMethod.addParameter("client_id",  (String) ConfigurationReader.getProperty("customer_portal.client_id", ""))
            httpMethod.addParameter("client_secret",  (String) ConfigurationReader.getProperty("customer_portal.client_secret", ""))
            if(exception_code == DomainConstants.OAUTH_ERROR_CODE.INVALID_ACEESS_TOKEN || exception_code == DomainConstants.OAUTH_ERROR_CODE.ACCESS_TOKEN_EXPIRE) {
                httpMethod.addParameter("grant_type", DomainConstants.OAUTH_CONSTANTS.REFRESH_TOKEN)
                httpMethod.addParameter("refresh_token", getRefreshToken())
            } else {
                httpMethod.addParameter("grant_type", DomainConstants.OAUTH_CONSTANTS.AUTHORIZATION_CODE)
                httpMethod.addParameter("code", (String) ConfigurationReader.getProperty("customer_portal.auth_code", ""))
            }
            def result = execute(httpMethod).tokens
            accessToken = result.access_token
            refreshToken = result.refresh_token
            return result
        } catch (OAuthException ex) {
            if(ex.errorCode == DomainConstants.OAUTH_ERROR_CODE.INVALID_REFRESH_TOKEN) {
                return retrieveTokens(ex.errorCode)
            }
            throw ex
        }
    }

    private def execute(HttpMethod httpMethod) {
        HttpClient httpClient = new HttpClient()
        httpClient.executeMethod(httpMethod)
        JsonSlurper slurper = new JsonSlurper()
        if(httpMethod.statusCode == HttpStatus.SC_OK) {
            return slurper.parseText(httpMethod.getResponseBodyAsString())
        } else {
            def result = slurper.parseText(httpMethod.getResponseBodyAsString())
            throw new OAuthException(result.message, result.errorCode)
        }
    }

    private def executeApi(HttpMethod httpMethod) {
        try {
            httpMethod.addRequestHeader("access_token", getAccessToken())
            return execute(httpMethod)
        } catch (OAuthException ex) {
            if(ex.errorCode == DomainConstants.OAUTH_ERROR_CODE.ACCESS_TOKEN_EXPIRE || ex.errorCode == DomainConstants.OAUTH_ERROR_CODE.INVALID_ACEESS_TOKEN) {
                retrieveTokens(ex.errorCode)
                executeApi(httpMethod)
            } else {
                throw ex
            }
        }
    }

    /**
     * Customer Support
     */
    def getSupportMessages(Map params) {
        String url = getEndPoint("/rest/supportMessage/list?max=${params.max}&offset=${params.offset}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def addSupportMessage(Map params) {
        String url = getEndPoint("/rest/supportMessage/add")
        PostMethod method = new PostMethod(url)
        method.addParameter("message", params.message)
        method.addParameter("customerName", provisionAPIService.accountDetailsByLicense().displayName)
        return executeApi(method)
    }

    def addSupportMessageReply(Map params) {
        String url = getEndPoint("/rest/supportMessage/addReply")
        PostMethod method = new PostMethod(url)
        method.addParameter("messageId", params.messageId)
        method.addParameter("message", params.message)
        method.addParameter("responderName", provisionAPIService.accountDetailsByLicense().displayName)
        return executeApi(method)
    }

    /**
     * Custom Project
     */

    def getCustomProjects(Map params) {
        String url = getEndPoint("/rest/customProject/list?max=${params.max}&offset=${params.offset}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def saveCustomProject(Map params) {
        String url = getEndPoint("/rest/customProject/save")
        PostMethod method = new PostMethod(url)
        method.addParameter("type", ["web_design", "maintenance"].get(RandomUtils.nextInt() % 2))
        method.addParameter("name", ["gold", "sliver", "platinum"].get(RandomUtils.nextInt() % 3))
        return executeApi(method)
    }

    def getCustomProjectMilestones(projectId) {
        String url = getEndPoint("/rest/customProject/milestones?projectId=${projectId}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def approveProjectMilestone(id) {
        String url = getEndPoint("/rest/customProject/approveMilestone")
        PostMethod method = new PostMethod(url)
        method.addParameter("id", id)
        return executeApi(method)
    }

    def getCustomProjectDetails(projectId) {
        String url = getEndPoint("/rest/customProject/details?projectId=${projectId}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def addProjectDetailsFile(GrailsParameterMap params) {
        MultipartFile file = params.file
        String url = getEndPoint("/rest/customProject/addDetailsFile")
        PostMethod method = new PostMethod(url)
        Part[] parts = new Part[2];;
        parts[0]  = new FilePart("file", new ByteArrayPartSource(file.originalFilename, file.bytes))
        parts[1] = new StringPart("id", params.detailsId)
        MultipartRequestEntity entity = new MultipartRequestEntity(parts, method.getParams())
        method.setRequestEntity(entity)
        executeApi(method)
    }

    def getCustomProjectFiles(projectId) {
        String url = getEndPoint("/rest/customProject/projectFiles?projectId=${projectId}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def addProjectProjectFile(GrailsParameterMap params) {
        List<MultipartFile> files = AppUtil.request.getFiles("files")
        String url = getEndPoint("/rest/customProject/addProjectFiles")
        PostMethod method = new PostMethod(url)
        List<Part> parts = new ArrayList<Part>()
        parts.add new StringPart("id", params.projectId)
        parts.add new StringPart("description", params.description)
        files.each { MultipartFile file ->
            parts.add new FilePart("files", new ByteArrayPartSource(file.originalFilename, file.bytes))
        }
        MultipartRequestEntity entity = new MultipartRequestEntity(parts as Part[], method.getParams())
        method.setRequestEntity(entity)
        return executeApi(method)
    }

    def getProjectMessages(Map params) {
        String url = getEndPoint("/rest/customProject/projectMessages?max=${params.max}&offset=${params.offset}&projectId=${params.id}");
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def addProjectMessage(Map params) {
        String url = getEndPoint("/rest/customProject/addProjectMessage")
        PostMethod method = new PostMethod(url)
        method.addParameter("projectId", params.projectId)
        method.addParameter("message", params.message)
        method.addParameter("customerName", provisionAPIService.accountDetailsByLicense().displayName)
        return executeApi(method)
    }

    def addProjectMessageReply(Map params) {
        String url = getEndPoint("/rest/customProject/addProjectMessageReply")
        PostMethod method = new PostMethod(url)
        method.addParameter("projectId", params.projectId)
        method.addParameter("messageId", params.messageId)
        method.addParameter("message", params.message)
        method.addParameter("responderName", provisionAPIService.accountDetailsByLicense().displayName)
        return executeApi(method)
    }

    def getProjectSitemap(projectId) {
        String url = getEndPoint("/rest/customProject/sitemap?proejctId=${projectId}")
        GetMethod getMethod = new GetMethod(url)
        return executeApi(getMethod)
    }

    def saveProjectSitemap(Map params) {
        String url = getEndPoint("/rest/customProject/saveSitemap")
        PostMethod method = new PostMethod(url)
        method.addParameter("projectId", params.projectId)
        method.addParameter("sitemap", params.sitemap)
        return executeApi(method)
    }
}

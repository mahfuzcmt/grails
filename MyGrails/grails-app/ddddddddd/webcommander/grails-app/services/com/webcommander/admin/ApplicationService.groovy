package com.webcommander.admin

import com.webcommander.events.AppEventManager
import com.webcommander.oauth.OauthProviderService
import com.webcommander.oauth2.OAuthClient
import com.webcommander.util.StringUtil
import grails.gorm.transactions.Transactional

@Transactional
class ApplicationService {
    OauthProviderService oauthProviderService

    List<OAuthClient> getApplications(Map params) {
        return OAuthClient.createCriteria().list([max: params.max, offset: params.offset]) {
            order(params.sort ?: "id", params.dir ?: "asc")
        }
    }

    Integer getCount(Map params) {
        return OAuthClient.createCriteria().count({})
    }

    @Transactional
    OAuthClient save(Map params, String clientId = null) {
        OAuthClient client = params.id ? OAuthClient.get(params.id) : new OAuthClient();
        client.enabled = params.enabled ? true : false;
        client.name = params.name;
        client.displayName = params.displayName;
        client.redirectUrl = params.redirectUrl;
        client.description = params.description;
        if(!client.uuid) {
            client.uuid = StringUtil.uuid
        }
        if(!client.clientId) {
           client.clientId = clientId ?: oauthProviderService.getToken()
        }
        if(!client.clientSecret) {
           client.clientSecret = oauthProviderService.getToken()
        }
        client.save()
        if (!client.hasErrors()) {
            return client
        }
        return null
    }

    @Transactional
    def delete(Long id) {
        OAuthClient client = OAuthClient.get(id);
        AppEventManager.fire("before-application-delete", [id]);
        client.delete()
        return true
    }
}

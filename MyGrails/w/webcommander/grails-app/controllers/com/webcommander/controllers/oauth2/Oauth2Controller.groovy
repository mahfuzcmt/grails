package com.webcommander.controllers.oauth2

import com.webcommander.admin.*
import com.webcommander.common.AuthenticationService
import com.webcommander.constants.DomainConstants
import com.webcommander.oauth.OauthProviderService
import com.webcommander.oauth2.OAuthAccess
import com.webcommander.oauth2.OAuthClient
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.RestProcessor
import com.webcommander.util.security.SimpleEncrypter

class Oauth2Controller extends RestProcessor{
    OAuthClient client
    def credentials
    OauthProviderService oauthProviderService
    AuthenticationService authenticationService
    RoleService roleService
    CustomerService customerService
    ApplicationService applicationService

    def auth() {
        if (credentials[DomainConstants.OAUTH_CONSTANTS.RESPONSE_TYPE] != "code") {
            throw new OAuthException("invalid.auth.request", DomainConstants.OAUTH_ERROR_CODE.INVALID_REQUEST)
        }
        String resourceOwnerType = params.resource_owner_type ?: DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR
        if(!(resourceOwnerType in DomainConstants.RESOURCE_OWNER_TYPES.values())) {
            throw new OAuthException("invalid.resource.owner", DomainConstants.OAUTH_ERROR_CODE.INVALID_REQUEST)
        }
        if (resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR && !session.admin) {
            session."${DomainConstants.SESSION_ATTR_KEYS.AFTER_LOGIN_URL}" = "${request.forwardURI}?${request.queryString}"
            flash.param = [redirectUrl: "${request.forwardURI}?${request.queryString}"]
            redirect(uri: "/admin");
        } else if(resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.CUSTOMER && !session.customer) {
            flash.param = [referer: "${request.forwardURI}?${request.queryString}"]
            redirect(uri: "/customer/login");
        } else {
            session.csrf_token = oauthProviderService.getToken();
            render (view: "/admin/oauth2/auth", model: [client: client, scope: credentials[DomainConstants.OAUTH_CONSTANTS.SCOPE], resourceOwnerType: resourceOwnerType])
        }
    }

    def token() {
        OAuthAccess access = null;
        if(credentials[DomainConstants.OAUTH_CONSTANTS.CLIENT_SECRET] != client.clientSecret) {
            throw new OAuthException("invalid.client.secret", "invalid_client_secret")
        }
        if(credentials[DomainConstants.OAUTH_CONSTANTS.GRANT_TYPE] == DomainConstants.OAUTH_CONSTANTS.AUTHORIZATION_CODE) {
            access = OAuthAccess.findByCodeAndRefreshTokenIsNull(credentials[DomainConstants.OAUTH_CONSTANTS.CODE])
        } else if (credentials[DomainConstants.OAUTH_CONSTANTS.GRANT_TYPE] == DomainConstants.OAUTH_CONSTANTS.REFRESH_TOKEN) {
            access = OAuthAccess.findByClientAndRefreshToken(client, credentials[DomainConstants.OAUTH_CONSTANTS.REFRESH_TOKEN])
        }
        if(access == null) {
            throw  new OAuthException("invalid.grant.request", "invalid_grant_request")
        }
        access = oauthProviderService.generateToken(access)
        rest tokens: [
            (DomainConstants.OAUTH_CONSTANTS.ACCESS_TOKEN): access.accessToken,
            (DomainConstants.OAUTH_CONSTANTS.REFRESH_TOKEN): access.refreshToken
        ]
    }

    def consent() {
        String redirectUrl;
        URI redirectURI = new URI(credentials[DomainConstants.OAUTH_CONSTANTS.REDIRECT_URI]);
        if(session.csrf_token != params.csrf_token) {
            throw new OAuthException("invalid.auth.request", DomainConstants.OAUTH_ERROR_CODE.INVALID_REQUEST)
        }
        if(params.allowed) {
            String resourceOwnerType = params.resource_owner_type ?: DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR
            Operator operator = null;
            Customer customer = null;
            if(resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR) {
                operator = Operator.get(session.admin)
            } else  if(DomainConstants.RESOURCE_OWNER_TYPES.CUSTOMER) {
                customer = Customer.get(session.customer)
            }
            OAuthAccess access = oauthProviderService.getNewOAuthAccess(client, operator, customer)
            redirectUrl = (redirectURI.scheme ? redirectURI.scheme + "://" : "") + (redirectURI.host ?: "") +  (redirectURI.path ?: "") + "?" + (redirectURI.query ? redirectURI.query + "&code=" : "code=" ) +  access.code
        } else {
            redirectUrl = "#error=${DomainConstants}";
        }
        redirect(url: redirectUrl)
    }

    def mobileAuth() {
        String email = params.email, password = params.password, deviceId = params.device_id, resourceOwnerType = params.resource_owner_type ?: DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR;
        Operator operator = null;
        Customer customer = null;
        Boolean isValid = (resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.OPERATOR && (operator = authenticationService.verifyUser(email, password))) ||
                (resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.CUSTOMER && (customer = customerService.verifyCustomer(email, password).customer)) ||
                resourceOwnerType == DomainConstants.RESOURCE_OWNER_TYPES.ANONYMOUS

        if(!isValid) {
            throw new OAuthException("invalid.resource.owner", DomainConstants.OAUTH_ERROR_CODE.INVALID_RESOURCE_OWNER)
        }
        Long time = new SimpleEncrypter(params.long("time")).getInfo()
        Long differ = System.currentTimeMillis() - time;
        if(differ > 300000 || !deviceId) {
            throw new OAuthException("invalid.request", DomainConstants.OAUTH_ERROR_CODE.INVALID_REQUEST)
        }

        OAuthClient client = OAuthClient.findByClientId(deviceId);
        if(!client) {
            Map map = [
                    name: deviceId,
                    displayName: params.name ?: "Mobile Device",
                    enabled: true,
                    redirectUrl: "http://localhost"
            ]
           client = applicationService.save(map, deviceId)
        }
        OAuthAccess access = oauthProviderService.getNewOAuthAccess(client, operator, customer)
        Map responseMap = [client_secret: client.clientSecret, code: access.code, redirect_uri: client.redirectUrl]
        if(operator) responseMap.permissions = roleService.getPermissions(operator.id)
        rest responseMap
    }
}

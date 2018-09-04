package com.webcommander.oauth

import com.webcommander.admin.Customer
import com.webcommander.admin.Operator
import com.webcommander.annotations.Initializable
import com.webcommander.events.AppEventManager
import com.webcommander.oauth2.OAuthAccess
import com.webcommander.oauth2.OAuthClient
import com.webcommander.util.StringUtil
import grails.gorm.transactions.Transactional

@Initializable
class OauthProviderService {

    static void initialize() {
        AppEventManager.on("before-application-delete", { id ->
            List<OAuthAccess> accesses = OAuthAccess.createCriteria().list {
                eq("client.id", id)
            }
            accesses*.delete()
        })

        AppEventManager.on("before-operator-delete", { id ->
            OAuthAccess.createCriteria().list {
                eq("operator.id", id)
            }*.delete()
        })

        AppEventManager.on("before-customer-delete", { id ->
            OAuthAccess.createCriteria().list {
                eq("customer.id", id)
            }*.delete();
        })
    }

    def getToken(String tokenBase = StringUtil.uuid) {
        return "${tokenBase}${System.currentTimeMillis()}".encodeAsMD5()
    }

    @Transactional
    OAuthAccess getNewOAuthAccess(OAuthClient client, Operator operator, Customer customer = null) {
        OAuthAccess access = OAuthAccess.findByClientAndOperatorAndCustomer(client, operator, customer) ?: new OAuthAccess(client: client, operator: operator, customer: customer);
        access.code = getToken();
        access.accessToken = null;
        access.refreshToken = null;
        access.save()
        return access
    }

    @Transactional
    OAuthAccess generateToken(OAuthAccess access) {
        access.refreshToken = getToken(access.code)
        access.accessToken = getToken(access.refreshToken)
        access.save()
        return access
    }

    OAuthAccess generateToken(OAuthClient client, Operator operator, Customer customer = null) {
        OAuthAccess access = getNewOAuthAccess(client, operator, customer)
        return generateToken(access)
    }

    OAuthAccess generateToken(Long clientId, Long operatorId = null, Long customerId = null) {
        generateToken(OAuthClient.get(clientId), operatorId ? Operator.get(operatorId) : null, customerId ? Customer.get(customerId) : null)
    }

    OAuthAccess getOAuthAccess(String accessToken) {
        return OAuthAccess.createCriteria().get {
            eq("accessToken", accessToken)
        }
    }
}

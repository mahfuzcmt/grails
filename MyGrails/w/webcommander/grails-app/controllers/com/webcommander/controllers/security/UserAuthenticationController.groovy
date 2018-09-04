package com.webcommander.controllers.security

import com.webcommander.admin.Operator
import com.webcommander.admin.UserService
import com.webcommander.common.AuthenticationService
import com.webcommander.common.CommanderMailService
import com.webcommander.manager.LicenseManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.security.InformationEncrypter
import grails.converters.JSON
import grails.util.Holders

import javax.servlet.http.Cookie

class UserAuthenticationController {

    AuthenticationService authenticationService
    UserService userService
    CommanderMailService commanderMailService
    ProvisionAPIService provisionAPIService

    def login() {
        Operator user = authenticationService.verifyUser(params.email, params.password)
        if(user) {
            authenticationService.registerAdminSession(user, params)
            if(params.redirectUrl) {
                redirect(uri: params.redirectUrl)
            } else {
                redirect(controller: "adminBase", action: "dashboard")
            }
        } else {
            flash.model = [user: user, error: g.message(code: "email.password.not.match")]
            redirect(uri: "/admin")
        }
    }

    def login_embed() {
        Operator user = authenticationService.verifyUser(params.email, params.password)
        if(user) {
            authenticationService.registerAdminSession(user, params)
            render([status: "success"] as JSON)
        } else {
            render([status: "error"] as JSON)
        }
    }

    def logout() {
        authenticationService.clearAdminSession(session)
        Cookie adminRemember = request.getCookies().find {it.name.startsWith("remember-admin")}
        if (adminRemember) {
            adminRemember.maxAge = 0
            adminRemember.path = "/"
            response.addCookie(adminRemember)
        }
        Cookie ckiSession = request.getCookies().find { it.name.startsWith("ckisession") }
        if(ckiSession) {
            ckiSession.maxAge = 0
            ckiSession.path = "/"
            response.addCookie(ckiSession)
        }
        if(LicenseManager.isProvisionActive()) {
            redirect(uri: "/")
        } else {
            render view: "/admin/login.gsp", model: [d: true]
        }
    }

    def resetPassword(){
        if(params.token) {
            render view: "/admin/newPassword.gsp", model: [token: params.token]
        } else {
            render view: "/admin/resetPassword.gsp", model: [d: true]
        }
    }

    def passwordResetLink() {
        String message = ""
        String error = ""
        String email = params.userEmail
        Operator user = Operator.findByEmail(email)
        if (!user) {
            message = g.message(code: 'email.with.password.reset.link.sent')
        } else {
            InformationEncrypter rsa = new InformationEncrypter()
            rsa.hideInfo("" + user.id)
            rsa.hideInfo("" + user.email)
            def resetPasswordLink = app.baseUrl() + "userAuthentication/resetPassword?token=" + rsa.toString().encodeAsURL()
            try {
                commanderMailService.sendResetPasswordMail(user, resetPasswordLink)
                message = g.message(code: 'email.with.password.reset.link.sent')
            }
            catch (Exception e) {
                error = g.message(code: 'could.not.send.mail.try.later')
            }
        }
        flash.model = [message: message, error: error]
        redirect(uri: "/admin")
    }

    def changePassword() {
        try {
            String token = params.token
            def userId
            if(LicenseManager.isProvisionActive()) {
                def result = provisionAPIService.emailByToken(token)
                provisionAPIService.changePassword(result.email, params.password.trim().encodeAsMD5(), params.token.toString())
            } else {
                InformationEncrypter auth = new InformationEncrypter(token, 60000 * 60 * 48)
                def infos = auth.hiddenInfos
                userId = infos.get(0).toInteger()
                userService.updatePassword(userId, params.password)
            }
            String message = g.message(code: 'password.change.success')
            flash.model = [message: message]
            redirect(uri: "/admin")
        } catch(Throwable t) {
            String error = g.message(code: 'unable.update.password.requested.link.expired')
            flash.param = [token: params.token]
            flash.model = [error: error]
            redirect(uri: "/userAuthentication/resetPassword")
        }
    }

    def silentLogin() {
        try {
            Map entity = provisionAPIService.entityByToken(params.token)
            Operator operator
            if(entity?.email?.startsWith("implementer@")) {
                operator = Operator.findByEmail("implementer@webcommander")
            } else {
                operator = Operator.findByUuid(entity.entityUUID)
                if(operator && operator.email != entity.email) {
                    operator.email = entity.email
                    operator.save()
                }
            }

            if(operator && operator.isActive) {
                authenticationService.registerAdminSession(operator, [:])
                if (params.afterLoginUrl) {
                    String afterLoginUrl = params.afterLoginUrl.toString().decodeURL()
                    redirect(url: afterLoginUrl)
                } else {
                    redirect(controller: "adminBase", action: "dashboard")
                }
            } else {
                render view: "/admin/disabled_access", model: [topType: 'operator', message: g.message(code: 'operator.is.inactive')]
            }
        } catch (OAuthException ex) {
            println("Login OAuthException: " + ex.getMessage())
            Cookie ckiSession = request.getCookies().find { it.name.startsWith("ckisession") }
            if(ckiSession) {
                ckiSession.maxAge = 0
                ckiSession.path = "/"
                response.addCookie(ckiSession)
            }
            String url =  LicenseManager.ssoURL()
            redirect(url: url)
        }
    }
}
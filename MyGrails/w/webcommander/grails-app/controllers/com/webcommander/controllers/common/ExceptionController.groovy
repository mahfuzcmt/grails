package com.webcommander.controllers.common

import com.webcommander.constants.DomainConstants
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.util.URLUtil
import grails.converters.JSON

class ExceptionController extends RestProcessor {

    def handle500() {
        Throwable t = request.exception;
        def cause = t?.cause
        Map messageMap
        if(cause instanceof OAuthException) {
            rest status: "error", code: cause.statusCode, message: cause.getMessage(), errorCode: cause.getErrorCode()
            return;
        } else if(cause instanceof ApiException) {
            rest status: "error", code: cause.statusCode, message: cause.getMessage()
            return;
        } else if(cause instanceof ApplicationRuntimeException) {
            messageMap = [code: t.cause.message, args: t.cause.args]
        } else {
            messageMap = [code: (request.'javax.servlet.error.message' ?: "unexpected.error.occurred"), args: request.'javax.servlet.error.params']
        }
        request.withMime {
            html {
                if(params.jsonashtml) {
                    render([status: "error", code: 500, message: g.message(messageMap)] as JSON)
                    return;
                }
                render view: "/static/error_html", model: [message: g.message(messageMap)]
            }
            json {
                render([status: "error", code: 500, message: g.message(messageMap)] as JSON)
            }
        }
    }

    def handle404() {
        String message = request.'javax.servlet.error.message' ?: "requested.resource.not.available"
        request.withMime {
            html {
                if(request.xhr || URLUtil.fileExtension(request.forwardURI)) {
                    render view: "/static/error_html", model: [message: g.message(code: message)]
                } else if(request._404) {
                    render view: "/static/site_default_error", model: [dummy: true]
                } else {
                    def page404 = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "page404")
                    if(page404) {
                        request._404 = true
                        params.page = page404
                        forward(controller: "page", action: "view", params: params)
                    } else {
                        render view: "/static/site_default_error", model: [dummy: true]
                    }
                }
            }
            json {
                render([status: "error", code: 404, message: g.message(code: message)] as JSON)
            }
        }
    }

    def handle403() {
        String message = request.'javax.servlet.error.message' ?: "access.forbidden"
        request.withMime {
            html {
                if(request.xhr || URLUtil.fileExtension(request.forwardURI)) {
                    render view: "/static/error_html", model: [message: g.message(code: message)]
                } else if(request._403) {
                    render view: "/static/site_default_forbidden", model: [dummy: true]
                } else {
                    def page403 = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "page403")
                    if(page403) {
                        request._403 = true
                        params.page = page403
                        forward(controller: "page", action: "view", params: params)
                    } else {
                        render view: "/static/site_default_forbidden", model: [dummy: true]
                    }
                }
            }
            json {
                render([status: "error", code: 403, message: g.message(code: message)] as JSON)
            }
        }
    }
}

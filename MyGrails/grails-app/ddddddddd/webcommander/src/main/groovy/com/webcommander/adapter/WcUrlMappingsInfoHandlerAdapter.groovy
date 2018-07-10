package com.webcommander.adapter

import com.webcommander.converter.XML
import com.webcommander.rest.throwable.ApiException
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.CartManagerException
import com.webcommander.throwables.sso.OAuthException
import com.webcommander.util.AppUtil
import grails.artefact.controller.support.ResponseRenderer
import grails.converters.JSON
import grails.core.GrailsControllerClass
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.mapping.mvc.GrailsControllerUrlMappingInfo
import org.grails.web.mapping.mvc.UrlMappingsInfoHandlerAdapter
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException

class WcUrlMappingsInfoHandlerAdapter extends UrlMappingsInfoHandlerAdapter implements ResponseRenderer {
    private ApplicationTagLib _g;
    private ApplicationTagLib getG() {
        return _g ?: (_g = applicationContext.getBean(ApplicationTagLib))
    }

    @Override
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            return super.handle(request, response, handler)
        } catch (InvocationTargetException exc) {
            Throwable cause = exc.targetException
            if(cause == null || !(handler instanceof GrailsControllerUrlMappingInfo)) {
                throw exc
            }
            Map params = AppUtil.params
            GrailsControllerClass controllerClass = handler.controllerClass
            String controllerName = controllerClass.clazz.simpleName;
            Boolean isApiController = controllerName.startsWith("Api");
            if(isApiController || controllerName == "Oauth2Controller") {
                Map responseMap = [:]
                Integer status = 400
                if(cause instanceof OAuthException){
                    responseMap = [status: "error", errorCode: cause.getErrorCode(), message: cause.getMessage()]
                } else if(cause instanceof ApiException) {
                    status = cause.statusCode ?: status
                    responseMap = [status: "error", message: cause.getMessage()]
                } else if(cause instanceof IllegalArgumentException || cause instanceof ValidationException) {
                    responseMap = [status: "error", message: g.message(code: "illegal.arguments")]
                } else if(cause instanceof CartManagerException) {
                    responseMap = [status: "error", message: cause.message]
                } else if(cause instanceof ApplicationRuntimeException) {
                    responseMap = [status: "error", message: cause.localizedMessage]
                }else {
                    responseMap = [status: "error", message: g.message(code: "invalid.api.request")]
                }
                response.setStatus(status);
                def processed
                if(params.format) {
                    if(params.format == "xml") {
                        processed = response as XML
                        response.contentType = "text/xml"
                    } else if(params.format == "json") {
                        response.contentType = "application/json"
                        processed = responseMap as JSON
                    }
                } else {
                    request.withMime {
                        html {
                            processed = responseMap as JSON
                        }
                        xml {
                            processed = responseMap as XML
                            response.contentType = "text/xml"
                        }
                        json {
                            response.contentType = "application/json"
                            processed = responseMap as com.webcommander.converter.json.JSON
                        }
                    }
                }
                return render(processed);
            }
            if(cause instanceof ApplicationRuntimeException) {
                ApplicationRuntimeException appError = cause;
                Integer statusCode = appError.errorCode ?: 400
                return request.withMime {
                    html {
                        response.setStatus(statusCode)
                        return new ModelAndView("/static/error_html", [message: appError.localizedMessage]);
                    }
                    json {
                        response.setStatus(statusCode)
                        return render([status: statusCode == 401 ? "unauthorized" : "error", code: statusCode, message: appError.localizedMessage] as JSON)
                    }
                }
            }
            throw exc
        }
    }
}

package com.webcommander

import com.webcommander.helper.ApiUrlMappingHelper
import com.webcommander.manager.LicenseManager
import grails.util.Holders

class UrlMappings {

    static excludes = ["/applet/**", "/css/**", "/video-js/**", "/font/**", "/galleries/**", "/images/**", "/js/**", "/resources/**", "/redactor/**", "/plugins/**", "GET:/pub/**", "GET:/template/**"]
    static mappings = {
        "/"{
            String ssoHost = Holders.config.webcommander.sso.host ?:""
            controller = {
                if (LicenseManager.isProvisionActive() && ssoHost.contains(request.serverName)) {
                    return "adminBase"
                }
                return  "page"
            }

            action = {
                if (LicenseManager.isProvisionActive() && ssoHost.contains(request.serverName)) {
                    return "dashboard"
                }
                return "view"
            }
        }

        "/rest/$_controller/$_action(.$format)?"(parseRequest: true) {
            controller = {
                ApiUrlMappingHelper.getController(params)
            }

            action = {
                ApiUrlMappingHelper.getAction(params)
            }
        }

        "/rest/admin/$_controller/$_action(.$format)?" {
            controller = {
                ApiUrlMappingHelper.getController(params, true)
            }

            action = {
                ApiUrlMappingHelper.getAction(params, true)
            }
        }

        "/rest/$_controller/$_child/$_action(.$format)?" {
            controller = {
                ApiUrlMappingHelper.getController(params)
            }

            action = {
                ApiUrlMappingHelper.getAction(params)
            }
        }

        "/rest/admin/$_controller/$_child/$_action(.$format)?" {
            controller = {
                ApiUrlMappingHelper.getController(params, true)
            }

            action = {
                ApiUrlMappingHelper.getAction(params, true)
            }
        }

        "/admin"(controller: "adminBase", action: "dashboard")
        "/editor"(controller: "page", action: "renderPage")
        "/$controller/$action"()

        "/$repository" (controller: "remoteRepository") {
            action = [MOVE: "doMove", PUT: "doPut", PROPFIND: "doPropfind", MKCOL: "doMkdir", DELETE: "doDelete"]
            constraints {
                repository(inList: ["pub", "template"])
            }
        }

        "/$repository/$path**" (controller: "remoteRepository") {
            action = [MOVE: "doMove", PUT: "doPut", PROPFIND: "doPropfind", MKCOL: "doMkdir", DELETE: "doDelete"]
            constraints {
                repository(inList: ["pub", "template"])
            }
        }

        "/$page"(controller: "page", action: "view")
        "/$file.$ext"(controller: "page", action: "seoRoot") {
            fileName = {
                params.file + "." + params.ext
            }
        }
        "/favicon.ico"(redirect: "favicon/site.ico")
        "/pagecss/$type/$id/style.css"(controller: "page") {  //type -> layout | page | dock | widget | pageAll | layoutAll
            action = {
                params.type + "Css"
            }
        }
        "/pagejs/$container/$id/page.js"(controller: "page", action: "pageJs")
        "/product/$url"(controller: "page", action: "product")
        "/category/$url"(controller: "page", action: "category")
        "/article/$url"(controller: "page", action: "article")
        "/subscription"(controller: "page", action: "subscription")
        "/unsubscription"(controller: "page", action: "unsubscription")
        "/orderComment"(controller: "page", action: "orderComment")

        "500" (controller: "exception", action: "handle500")
        "404" (controller: "exception", action: "handle404")
    }
}

package com.webcommander.controllers.rest.admin.content

import com.webcommander.Page
import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.constants.DomainConstants
import com.webcommander.content.PageService
import com.webcommander.rest.throwable.ApiException
import com.webcommander.util.RestProcessor

class ApiPageAdminController extends RestProcessor {
    PageService pageService
    ConfigService configService

    @Restriction(permission = "page.view.list")
    def list() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<Page> pages = pageService.getPages(params)
        rest(pages: pages)
    }

    @Restriction(permission = "page.view.list")
    def info() {
        Page page = pageService.getPage(params.long("id"))
        rest page: page
    }

    @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    def urlUpdate() {
        if(pageService.updateProperties(params.long("id"), "url", params.url)) {
            rest status: "success"
        } else {
            throw new ApiException("url.update.failed")
        }

    }
    @Restriction(permission = "page.edit.properties", params_exist = "id", entity_param = "id", domain = Page, owner_field = "createdBy")
    def setLandingPage() {
        Page page = pageService.getPage(params.long("id"))
        def config = [
            [
                type: DomainConstants.SITE_CONFIG_TYPES.GENERAL,
                configKey: "landing_page",
                value: page.url
            ]
        ]
        if (configService.update(config)) {
            rest([status: "success", message: g.message(code: "page.set.as.landing")])
        } else {
            throw new ApiException("page.not.set.as.landing")
        }
    }
}

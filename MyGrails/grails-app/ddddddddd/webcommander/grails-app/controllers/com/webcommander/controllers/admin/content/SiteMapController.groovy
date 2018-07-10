package com.webcommander.controllers.admin.content

import com.webcommander.admin.SiteMapService
import grails.converters.JSON

class SiteMapController {
    SiteMapService siteMapService

    def loadAppView() {

        Map sitemap = siteMapService.readFile();
        render (view: "/admin/sitemap/siteMap", model:[sitemap: sitemap])
    }

    def showCreatePopUp() {
        render view: "/admin/sitemap/sitemapPopUp"
    }

    def create() {
        String result = siteMapService.createSiteMap(params)
        if (result) {
            render([status: "success", message: g.message(code: "sitemap.generation.succssessful"), xml: result] as JSON);
        } else {
            render([status: "error", message: g.message(code: "sitemap.generation.fail")] as JSON);
        }
    }

    String loadXml() {
        Map result = siteMapService.readFile()
        render result.sitemap
    }

    def saveXml() {
        boolean result = siteMapService.createFile(params.data)
        if (result) {
            render([status: "success", message: g.message(code: "sitemap.generation.succssessful"), xml: result] as JSON);
        } else {
            render([status: "error", message: g.message(code: "sitemap.generation.fail")] as JSON);
        }
    }
}

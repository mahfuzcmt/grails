package com.webcommander.plugin.my_shopping.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.listener.SessionManager
import com.webcommander.plugin.my_shopping.MyShoppingService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class MyShoppingAdminController {
    MyShoppingService myShoppingService

    @License(required = "allow_myshopping_feature")
    def loadAppView() {
        render(view: "/plugins/my_shopping/admin/appView")
    }

    def myShoppingCategoryTree() {
        List list = myShoppingService.excelFileToMap()
        render(list as JSON)
    }

    def categoryTree() {
        String type = params.type ?: "";
        List children = myShoppingService.getCategoryInfoAsTree(type)
        render(children as JSON)
    }

    def mapCategory() {
        Boolean result = myShoppingService.mapCategory(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "mapping.failure")] as JSON)
        }
    }

    def removeMapping() {
        Boolean result = myShoppingService.removeMapping(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "remove.mapping.failure")] as JSON)
        }
    }

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.MY_SHOPPING)
        render(view: "/plugins/my_shopping/admin/config", model: [config: config])
    }

    @License(required = "allow_myshopping_feature")
    def download() {
        def xml =  myShoppingService.getXmlFeed()
        String fileName = "MyShopping-Product-Feed(" + new Date().gmt().toZone(session.timezone).toString() + ").xml"
        String path = SessionManager.protectedTempFolder.absolutePath
        File file = new File(path, "Product-Feed")
        file.write(xml)
        response.setHeader("Content-disposition", "attachment; filename=\"${fileName}\"")
        response.setHeader("Content-Type", "text/xml")
        response.outputStream << file.bytes
        response.outputStream.flush()
    }
}

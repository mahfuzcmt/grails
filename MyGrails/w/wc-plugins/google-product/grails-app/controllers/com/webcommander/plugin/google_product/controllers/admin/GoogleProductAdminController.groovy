package com.webcommander.plugin.google_product.controllers.admin

import com.webcommander.authentication.annotations.License
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.google_product.GoogleProductService
import com.webcommander.plugin.google_product.constants.Constants
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.listener.SessionManager

class GoogleProductAdminController {
    GoogleProductService googleProductService

    def updateCategory() {
        Boolean result = googleProductService.updateCategory()
        if (result) {
            render([status: "success", message: g.message(code: "google.category.update.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "google.category.update.failure")] as JSON)
        }
    }

    @License(required = "allow_google_product_feature")
    def loadAppView() {
        render(view: "/plugins/google_product/admin/appView")
    }

    def googleCategoryTree() {
        def map = [:]
        List list = googleProductService.excelFileToMap()
        render(list as JSON)
    }

    def categoryTree() {
        String type = params.type ?: "";
        List children = googleProductService.getCategoryInfoAsTree(type)
        render(children as JSON)
    }

    def mapCategory() {
        Boolean result = googleProductService.mapCategory(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "mapping.failure")] as JSON)
        }
    }

    def removeMapping() {
        Boolean result = googleProductService.removeMapping(params);
        if (result) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "remove.mapping.failure")] as JSON)
        }
    }

    def loadConfig() {
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GOOGLE_PRODUCT)
        Map mappingConfig = AppUtil.getConfig("google_variation_mapping") ?: [:];
        Map googleVariations = Constants.GOOGLE_VARIATION
        render(view: "/plugins/google_product/admin/config", model: [config: config, googleVariations: googleVariations, mapping: mappingConfig])
    }

    def saveConfigurations() {
        if (googleProductService.saveConfigurations(params)) {
            render([status: "success", message: g.message(code: "setting.updated.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "setting.not.updated")] as JSON)
        }
    }

    @License(required = "allow_google_product_feature")
    def feed() {
        def xml = googleProductService.getXmlFeed()
        render(text: xml, contentType: "text/xml", encoding: "UTF-8")
    }

    @License(required = "allow_google_product_feature")
    def download() {
        def xml = googleProductService.getXmlFeed()
        String fileName = "Google-Product-Feed(" + new Date().gmt().toZone(session.timezone).toString() + ").xml"
        String path = SessionManager.protectedTempFolder.absolutePath
        File file = new File(path, "Product-Feed")
        file.write(xml)
        response.setHeader("Content-disposition", "attachment; filename=\"${fileName}\"")
        response.setHeader("Content-Type", "text/xml")
        response.outputStream << file.bytes
        response.outputStream.flush()
    }
}
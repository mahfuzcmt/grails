package com.webcommander.controllers.admin.design

import com.webcommander.admin.ConfigService
import com.webcommander.authentication.annotations.License
import com.webcommander.common.FileService
import com.webcommander.constants.DomainConstants
import com.webcommander.design.TemplateService
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.PathManager
import com.webcommander.models.TemplateData
import com.webcommander.provision.DeploymentHelperService
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.util.AppUtil
import com.webcommander.util.ZipUtil
import grails.converters.JSON

import java.nio.charset.StandardCharsets
import java.util.zip.ZipOutputStream

class TemplateAdminController {

    TemplateService templateService
    TemplateInstallationService templateInstallationService
    ConfigService configService
    ProvisionAPIService provisionAPIService
    TemplateDataProviderService templateDataProviderService
    FileService fileService
    DeploymentHelperService deploymentHelperService

    def loadAppView() {
        Integer max = params.int("max") ?: 6
        Integer offset = params.int("offset") ?: 0
        def templates = templateDataProviderService.getAllTemplate(max, offset, "", "")
        def typesAndCategories = templateDataProviderService.getTemplateTypesAndCategories()
        render(view: "/admin/template/appView", model: [max: max, offset: offset, typesAndCategories: typesAndCategories, templates: templates])
    }

    def leftPanel() {
        List<Map> installedColors = templateService.getInstalledColors()
        String installedColor = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "template_color")
        render(view: "/admin/template/leftPanel", model: [installedColors: installedColors, installedColor: installedColor])
    }

    def reloadTemplate() {
        Integer max = params.int("max")
        Integer offset = params.int("offset")
        def templates = templateDataProviderService.getAllTemplate(max, offset, params.template_type, params.template_category)
        render(view: "/admin/template/thumbView", model: [templateList: templates.templateList, totalCount: templates.totalCount, max: max, offset: offset])
    }

    @License(required = "allow_template_modification_feature")
    def installTemplatePopup() {
        Map templateDetails = templateDataProviderService.getTemplateDetails(params.id)

        Map missingPlugins = templateInstallationService.getMissingPlugins(templateDetails.liveURL)
        if (missingPlugins.systemUndefinedPlugins || missingPlugins.installablePlugins) {
            render(view: "/admin/template/pluginMissingPopup", model: missingPlugins)
        } else {
            render(view: "/admin/template/installTemplatePopup")
        }
    }

    def installMissingPlugins() {
        Boolean result = true
        if (templateInstallationService.installablePlugin) {
            result = deploymentHelperService.installPlugins(templateInstallationService.installablePlugin)
        }
        if (result){
            install()
        } else {
            render([status: "error", message: g.message(code: "plugins.can.not.install")] as JSON)
        }
    }

    @License(required = "allow_template_modification_feature")
    def install() {
        Map templateDetails = templateDataProviderService.getTemplateDetails(params.id)
//        templateService.validate(templateDetails)
        Boolean result
        try {
            result = templateInstallationService.install(params, templateDetails)
        } catch (Throwable t) {
            log.error t.getMessage(), t
        }
        if (result) {
            render([status: "success", message: g.message(code: "template.installed.successfully")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "template.could.not.installed")] as JSON)
        }
    }

    @License(required = "allow_template_modification_feature")
    def changeColor() {
        if (params.color && templateService.changeColor(params.color)) {
            render([status: "success", message: g.message(code: "color.change.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "color.change.failed")] as JSON)
        }
    }

    def changeTemplateContainerClass() {
        if (params.templateContainerClass && templateService.changeTemplateContainerClass(params.templateContainerClass)) {
            render([status: "success"] as JSON)
        } else {
            render([status: "error", message: g.message(code: "operation.failed")] as JSON)
        }
    }

    def setDefaultContent() {
        Map data = fileService.getSystemDefaultEmailTemplateData(params.id)
        render([status: "success", contentTxt: data.text, contentHtml: data.html] as JSON)
    }

    def preview() {
        if (params.liveURL) {
            session.template_preview_url = params.liveURL
            session.template_preview = true
            session.template_preview_color = params.color
        }
        redirect(url: "/")
    }

    def backupTemplate() {
        ZipOutputStream zipOutputStream = new ZipOutputStream(response.outputStream)
        TemplateData templateData = templateDataProviderService.getTemplateData()
        String jsonData = (templateData.toMap() as grails.converters.JSON).toString()
        templateData.resources.each {
            String path = PathManager.getResourceRoot(it)
            File file = new File(path)
            if (file.exists()) {
                ZipUtil.addToZip("template-data/${it}", file, zipOutputStream)
            }
        }
        ZipUtil.addStreamToZip("template-data/", new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8)), zipOutputStream, "data.json")
        File templateFolder = new File(servletContext.getRealPath("/template"))
        ZipUtil.addToZip("template/", templateFolder, zipOutputStream)
        zipOutputStream.close()
        response.outputStream.flush()
        response.outputStream.close()
    }
}

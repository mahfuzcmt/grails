package com.webcommander.controllers.site

import com.webcommander.admin.ConfigService
import com.webcommander.admin.UserService
import com.webcommander.constants.DomainConstants
import com.webcommander.content.RemoteRepositoryService
import com.webcommander.installation.template.TemplateDataProviderService
import com.webcommander.installation.template.TemplateInstallationService
import com.webcommander.manager.PathManager
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.ZipUtil
import grails.converters.JSON

import java.util.zip.ZipOutputStream

class DeployController {
    TemplateDataProviderService templateDataProviderService
    TemplateInstallationService templateInstallationService
    RemoteRepositoryService remoteRepositoryService
    UserService userService
    ConfigService configService

    def zipTemplate() {
        OutputStream outputStream = response.outputStream
        ZipOutputStream  zipOutputStream = new ZipOutputStream(outputStream)
        File templateFolder = new File(PathManager.getRoot(remoteRepositoryService.getRelativeRepositoryBase("template")))
        ZipUtil.zipFolder(templateFolder, zipOutputStream)
        outputStream.flush()
        outputStream.close()
    }

    def templateData() {
        templateDataProviderService.provideData(response.outputStream)
        response.outputStream.flush()
        response.outputStream.close()
    }

    def installTemplate() {
        if(params.hiddenInfos[0] == params.liveURL) {
            if (templateInstallationService.install(params)) {
                render text: "Success"
            } else {
                render text: "Failed"
            }
        } else {
            render(text: "Unauthorized", status: 401)
        }
    }

    def initialData() {
        if(DomainConstants.DEPLOY_TOKEN == params.hiddenInfos[0]) {
            String status = "success"
            String message = "Initial data updated successfully"
            if(params.email) {
                try {
                    if (!userService.update(params, true)) {
                        throw new Exception()
                    }
                } catch (ApplicationRuntimeException ex) {
                    status = "error"
                    message = ex.localizedMessage
                } catch(Exception ex) {
                    status = "error"
                    message = "Operator could not be created"
                }
            }
            if(params.licenseCode) {
                List licenseConfig = []
                ["url", "clientId", "clientSecret", "code", "refreshToken", "accessToken", "licenseCode"].each {
                    licenseConfig.push([
                        type: DomainConstants.SITE_CONFIG_TYPES.LICENSE,
                        configKey: it,
                        value: params[it]
                    ])
                }
                configService.update(licenseConfig)
            }
            render([status: status, message: message] as JSON)
        } else {
            render(text: "Unauthorized", status: 401)
        }
    }
}

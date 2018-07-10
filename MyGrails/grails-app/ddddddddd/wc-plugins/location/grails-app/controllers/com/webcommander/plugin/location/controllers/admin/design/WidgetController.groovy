package com.webcommander.plugin.location.controllers.admin

import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.WcStaticResource
import com.webcommander.events.AppEventManager
import com.webcommander.listener.SessionManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.models.MockStaticResource
import com.webcommander.plugin.location.LocationAddress
import com.webcommander.plugin.location.LocationService
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CloudConfig
import grails.converters.JSON
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class WidgetController {
    LocationService locationService
    ImageService imageService
    def widgetService

    def editLocation() {
        render(view: "/plugins/location/admin/loadLocation", model: [:])
    }

    def locationShortConfig() {
        render(view: "/plugins/location/admin/loadLocationShort", model:  [noAdvance: true])
    }

    def saveLocationWidget() {
        MultipartFile uploadedImage = params.localImage ? request.getFile('localImage') : null
        if (uploadedImage?.originalFilename) {
            String fileName = uploadedImage.originalFilename;
            File tempLokation = SessionManager.publicTempFolder;
            String filePath = appResource.getWidgetTempRelativePath(type: 'location', uuid: params.uuid)
            MockStaticResource mockResource = new MockStaticResource(relativeUrl: filePath, resourceName: fileName)
            imageService.uploadImage(uploadedImage, null, mockResource)
            params.pin_url = "${appResource.getWidgetTempPath(type: 'location', uuid: params.uuid)}" + fileName
            AppEventManager.off("widget-" + params.uuid + "-before-save")
            AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
                String modifiedLocalUrl = "${appResource.getWidgetRelativeUrl(type: 'location', uuid: widget.uuid)}" + fileName
                String modifiedCloudUrl = ""
                String resourceId = "iw-" + widget.uuid
                WcStaticResource resource = WcStaticResource.findByResourceId(resourceId) ?: new WcStaticResource();
                resource.resourceId = resourceId
                resource.resourceName = fileName
                resource.relativeUrl = appResource.getWidgetRelativeUrl(type: 'location', uuid: widget.uuid)
                File targetFile = new File(Holders.servletContext.getRealPath(resource.relativeUrl));
                if (targetFile.exists()) {
                    targetFile.deleteDir()
                }
                if (!targetFile.parentFile.exists()) {
                    targetFile.parentFile.mkdirs()
                }
                File sourceFile = new File(Holders.servletContext.getRealPath(appResource.getWidgetTempPath(type: 'location', uuid: widget.uuid).toString()));
                if (sourceFile.exists()) {
                    try {
                        Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    } catch (IOException ex) {
                        ex.printStackTrace()
                        log.error("Source: " + sourceFile.toPath() + ", Destination: " + targetFile.toPath())
                    }
                    if(CloudStorageManager.isCloudEnable(NamedConstants.CLOUD_CONFIG.DEFAULT, resource)) {
                        CloudConfig cloudConfig = CloudStorageManager.uploadData(targetFile, NamedConstants.CLOUD_CONFIG.DEFAULT, resource.relativeUrl)
                        if(cloudConfig) {
                            resource.cloudConfig = cloudConfig
                            resource.baseUrl = cloudConfig.baseUrl
                            modifiedCloudUrl = "${resource.baseUrl}/${appResource.getWidgetCloudRelativeUrl(type: 'location', uuid: widget.uuid)}" + fileName
                        }
                    }
                }
                resource.save()
                Map params = JSON.parse widget.params
                params.pin_url = CloudStorageManager.isCloudEnable() ? modifiedCloudUrl : "/${modifiedLocalUrl}"
                widget.params = params as JSON;
            })
        }
        render(widgetService.saveAnyWidget("Location", params))
    }

    def autoComplete() {
        def locations = locationService.autoComplete(params)
        List<String>addresses = []

        locations.each {
            addresses.push(it.formattedAddress)
        }

        render(["query": "Unit", "suggestions": addresses] as JSON)
    }


    def matchSearchedValueWithExisting() {
        def count
        count = locationService.getSearchedValueCount(params)
        render([count: count])
    }
}
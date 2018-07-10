package com.webcommander.plugin.google_map.mixin_controller

import com.webcommander.constants.NamedConstants
import com.webcommander.content.WcStaticResource
import com.webcommander.events.AppEventManager
import com.webcommander.listener.SessionManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.models.MockStaticResource
import com.webcommander.webcommerce.CloudConfig
import grails.converters.JSON
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class WidgetController {
    def imageService
    def widgetService
    
    def editGoogleMap() {
        render(view: "/plugins/google_map/admin/loadGoogle", model: [:])
    }


    
    def googleMapShortConfig() {
        render(view: "/plugins/google_map/admin/loadGoogleShort", model:  [noAdvance: true])
    }

    
    def saveGoogleMapWidget() {
        MultipartFile uploadedImage = params.localImage ? request.getFile('localImage') : null
        if (uploadedImage?.originalFilename) {
            String fileName = uploadedImage.originalFilename;
            File tempLokation = SessionManager.publicTempFolder;
            String filePath = appResource.getWidgetTempRelativePath(type: 'google-map', uuid: params.uuid)//SessionManager.getRelativeTempPath() + "/google-map-widget/" + params.uuid + "/";
            MockStaticResource mockResource = new MockStaticResource(relativeUrl: filePath, resourceName: fileName)
            imageService.uploadImage(uploadedImage, null, mockResource);
            params.pin_url = "${appResource.getWidgetTempPath(type: 'google-map', uuid: params.uuid)}" + fileName
            AppEventManager.off("widget-" + params.uuid + "-before-save")
            AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
                String modifiedLocalUrl = "${appResource.getWidgetRelativeUrl(type: 'google-map', uuid: widget.uuid)}" + fileName//"resources/google-map-widget/" + widget.uuid + "/" + fileName
                String resourceId = "iw-" + widget.uuid
                WcStaticResource resource = WcStaticResource.findByResourceId(resourceId) ?: new WcStaticResource();
                resource.resourceId = resourceId
                resource.resourceName = fileName
                resource.relativeUrl = appResource.getWidgetRelativeUrl(type: 'google-map', uuid: widget.uuid)
                File targetFile = new File(Holders.servletContext.getRealPath(resource.relativeUrl));
                if (targetFile.exists()) {
                    targetFile.deleteDir()
                }
                if (!targetFile.parentFile.exists()) {
                    targetFile.parentFile.mkdirs()
                }
                File sourceFile = new File(Holders.servletContext.getRealPath(appResource.getWidgetTempPath(type: 'google-map', uuid: widget.uuid).toString()));
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
                            resource.baseUrl = cloudConfig.baseUrl//CloudStorageManager.upload(new File(targetFile, fileName), modifiedLocalUrl)
                        }
                    }
                }
                resource.save()
                Map params = JSON.parse widget.params
                params.pin_url = "/" + modifiedLocalUrl
                widget.params = params as JSON;
            })
        }
        render(widgetService.saveAnyWidget("GoogleMap", params))
    }
}
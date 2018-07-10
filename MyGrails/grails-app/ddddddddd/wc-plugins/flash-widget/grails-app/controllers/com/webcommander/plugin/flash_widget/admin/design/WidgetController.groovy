package com.webcommander.plugin.flash_widget.admin.design

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.common.FileService
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.util.AppUtil
import grails.converters.JSON
import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

class WidgetController {

    def widgetService

    def editFlash() {
        render(view: "/plugins/flash_widget/admin/loadFlash", model: [:])
    }


    def flashShortConfig() {
        render(view: "/plugins/flash_widget/admin/loadFlashShort",model: [advanceText: AppUtil.getBean(ApplicationTagLib).message(code: 'configure')])
    }


    def saveFlashWidget() {
        AppEventManager.off("widget-" + params.uuid + "-before-save")
        if(params.upload_type == "local") {
            if (params.local_file_path) {
                String originalName = params.local_file_name
                String filePath = params.local_file_path
                com.webcommander.controllers.admin.design.WidgetController widgetController = new com.webcommander.controllers.admin.design.WidgetController()
                widgetController.uploadWidgetLocalResource(params, originalName)
            }
        } else {
            AppEventManager.off("widget-" + params.uuid + "-before-save")
            AppEventManager.one("widget-" + params.uuid + "-before-save", "session-" + session.id, { widget ->
                File resource = new File(PathManager.getResourceRoot("flash-widget/${widget.uuid}/"))
                if(resource.exists()) {
                    resource.deleteDir()
                }
                CloudStorageManager.deleteData("${appResource.getWidgetRelativeUrl(uuid: widget.uuid, type: 'flash')}", NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
            })
        }
        render(widgetService.saveAnyWidget("Flash", params))
    }



    def tempFlash() {
        MultipartFile uploadedFile = request.getFile('local')
        String url = null;
        String filePath = null
        String uuid = params.uuid
        if (uploadedFile?.originalFilename) {
            String originalName = uploadedFile.originalFilename
            String widgetTempPath = AppUtil.getBean(AppResourceTagLib).getWidgetTempPath(type: 'flash', uuid: uuid)
            filePath = Holders.servletContext.getRealPath(widgetTempPath)
            FileService fileService = AppUtil.getBean(FileService);
            fileService.uploadFile(uploadedFile, null, originalName, null, filePath)
            url = widgetTempPath + originalName
        }
        render([url: url, flashWidget: "true", path: filePath, name: uploadedFile.originalFilename] as JSON)
    }
}
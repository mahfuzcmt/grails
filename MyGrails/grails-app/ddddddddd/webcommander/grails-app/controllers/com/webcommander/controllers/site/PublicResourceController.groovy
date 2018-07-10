package com.webcommander.controllers.site

import com.webcommander.AppResourceTagLib
import com.webcommander.common.FileService
import com.webcommander.config.StoreDetail
import com.webcommander.constants.NamedConstants


class PublicResourceController {

    FileService fileService
    final String SYSTEM_DEFAULT = NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT
    final String DEFAULT = NamedConstants.CLOUD_CONFIG.DEFAULT


    def templateImage() {
        InputStream inputStream
        String templateImagePath = "${AppResourceTagLib.IMAGES}/${AppResourceTagLib.LARGE_JPG}"
        if (fileService.isExistTemplateResource(templateImagePath)) {
            inputStream = fileService.readTemplateFileContentFromSystem(templateImagePath, SYSTEM_DEFAULT)
        } else {
            storeLogo()
        }
        OutputStream stream = response.outputStream
        stream << inputStream
        stream.flush()
    }


    def storeLogo() {
        InputStream inputStream
        StoreDetail storeDetail = StoreDetail.first()
        String relativePath = storeDetail.image ? storeDetail.getRelativeUrl() + storeDetail.resourceName : storeDetail.getRelativeUrl() + AppResourceTagLib.getDefaultImageRelativePath(null)
        if (fileService.isExistResource(relativePath)) {
            inputStream = fileService.readResourceFileContentFromSystem(relativePath, DEFAULT)
        }
        OutputStream stream = response.outputStream
        stream << inputStream
        stream.flush()
    }

}

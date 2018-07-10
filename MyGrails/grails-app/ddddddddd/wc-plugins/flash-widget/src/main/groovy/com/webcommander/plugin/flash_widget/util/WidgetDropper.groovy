package com.webcommander.plugin.flash_widget.util

import com.webcommander.AppResourceTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CloudStorageManager
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.commons.io.FileUtils

/**
 * Created by sajedur on 1/10/2014.
 */
class WidgetDropper {
    public static afterDropWidget(String uuid) {
        def appResource = AppUtil.getBean(AppResourceTagLib)
        String relativeUrl = appResource.getWidgetRelativeUrl(uuid: uuid, type: 'flash')
        File resourceFile = new File(Holders.servletContext.getRealPath(relativeUrl))
        if (resourceFile.exists()) {
            FileUtils.deleteDirectory(resourceFile)
        }
        CloudStorageManager.deleteData(""+appResource.getWidgetCloudRelativeUrl(uuid: uuid, type: 'flash'), NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
    }
}

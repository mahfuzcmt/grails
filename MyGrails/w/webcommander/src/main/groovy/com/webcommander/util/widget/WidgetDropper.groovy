package com.webcommander.util.widget

import com.webcommander.AppResourceTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.content.WcStaticResource
import com.webcommander.manager.CloudStorageManager
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.commons.io.FileUtils

class WidgetDropper {

    public static afterDropImageWidget(String uuid) {
        def appResource = AppUtil.getBean(AppResourceTagLib)
        String relativeUrl = appResource.getWidgetRelativeUrl(uuid: uuid, type: 'image')
        File resourceFile = new File(Holders.servletContext.getRealPath(relativeUrl))
        if (resourceFile.exists()) {
            FileUtils.deleteDirectory(resourceFile)
        }
        CloudStorageManager.deleteData(""+appResource.getWidgetCloudRelativeUrl(uuid: uuid, type: 'image'), NamedConstants.CLOUD_CONFIG.SYSTEM_DEFAULT)
        //WcStaticResource.findByResourceId("iw-" + uuid)?.delete()
    }

}

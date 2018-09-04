package com.webcommander.plugin.tab_accordion.util

import grails.util.Holders
import org.apache.commons.io.FileUtils

/**
 * Created by sajedur on 1/10/2014.
 */
class WidgetDropper {
    public static afterDropWidget(String uuid) {
        File resource = new File(Holders.servletContext.getRealPath("resources") + "/tab-accordion/" + uuid);
        if(resource.exists()) {
            FileUtils.deleteDirectory(resource);
        }
    }
}

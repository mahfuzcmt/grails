package com.webcommander.manager

import com.webcommander.AppResourceTagLib
import com.webcommander.plugin.PluginMeta
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CloudConfig
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Holders

class PathManager {
    //region ONLY FOR DEVELOPMENT
    private static String getDevelopmentRoot() {
        BuildSettings.BASE_DIR.absolutePath + "/"
    }

    static String getPluginSystemResourceRoot(PluginMeta plugin, String path = null) {
        return developmentRoot + "wc-plugins/$plugin.identifier/src/main/webapp/" + (path ?: '')
    }

    static String getPluginRestrictedResourceRoot(PluginMeta plugin, String path = null) {
        return developmentRoot + "wc-plugins/$plugin.identifier/src/main/webapp/WEB-INF/system-resources/" + (path ?: '')
    }
    //endregion

    //region FOR PUBLIC URL
    static String getStaticResourceURLRoot() {
        return "wc/" + AppUtil.appVersion + "/"
    }

    static String getResourceURLRoot() {
        return "resources/$TenantContext.currentTenant/"
    }
    //endregion

    //region DISK PATH
    static String getSystemResourceRoot(String path = null) {
        return rootPhysicalPath + staticResourceURLRoot + (path ?: '')
    }

    static String getResourceRoot(String path = null) {
        return rootPhysicalPath + resourceURLRoot + (path ?: '')
    }

    static String getRestrictedResourceRoot(String path = null) {
        return rootPhysicalPath + "WEB-INF/system-resources/" + (path ?: '')
    }

    static String getPluginRoot(String identifier, String path = null) {
        if(Environment.isWarDeployed()) {
            return getAppRootPath() + "WEB-INF/wc-plugins/" + identifier + "/" + (path ?: '')
        } else {
           return getAppRootPath() + "wc-plugins/" + identifier + "/src/main/webapp/WEB-INF/" + (path ?: '')
        }
    }

    static String getPluginPublicRoot(String identifier, String path = null){
       return getSystemResourceRoot("plugins/${identifier}/" + (path ?: ''))
    }

    static String getRoot(String path = null) {
        rootPhysicalPath + (path ?: '')
    }

    private static String getAppRootPath() {
        if(Environment.isWarDeployed()) {
            //WEB-INF/lib/webcommander.jar!/com/webcommander/manager/PathManager.class
            return Holders.servletContext?.getRealPath("") ?: new File(URLDecoder.decode(new URL(PathManager.class.getResource("PathManager.class").path).path, "UTF-8")).parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.absolutePath + "/"
        } else {
            return BuildSettings.BASE_DIR.absolutePath + "/"
        }
    }

    private static String getRootPhysicalPath() {
        if(Environment.isWarDeployed()) {
            //WEB-INF/lib/webcommander.jar!/com/webcommander/manager/PathManager.class
            return Holders.servletContext?.getRealPath("") ?: new File(URLDecoder.decode(new URL(PathManager.class.getResource("PathManager.class").path).path, "UTF-8")).parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.absolutePath + "/"
        } else {
            return BuildSettings.BASE_DIR.absolutePath + "/src/main/webapp/"
        }
    }

    static String getCustomRestrictedResourceRoot(String path = null) {
        rootPhysicalPath + "${AppResourceTagLib.WEB_INF_MODIFIABLE_RESOURCES}/$TenantContext.currentTenant/" + (path ?: '')
    }
    //endregion

    static String resourceURLGenerator(String resourceName, String extension = null){
        CloudConfig cloudConfig = CloudStorageManager.isCloudEnable()
        String url = ""
        if(cloudConfig && cloudConfig.baseUrl){
            url = "${cloudConfig.baseUrl}${resourceName}/${extension ?: ''}"
        }else{
            url = "/${resourceName}/${TenantContext.currentTenant}/${extension ?: ''}"
        }
        return url
    }
}
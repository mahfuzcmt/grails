package com.webcommander.models.blueprints

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CloudStorageManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.util.FileUtil
import com.webcommander.webcommerce.CloudConfig

abstract class AbstractStaticResource {

    CloudConfig cloudConfig

    static constraints = {
        cloudConfig(nullable: true)
    }

    static ApplicationTagLib _app
    static ApplicationTagLib getApp() {
        return _app ?: (_app = AppUtil.getBean(ApplicationTagLib))
    }

    static AppResourceTagLib _appResource
    static AppResourceTagLib getAppResource() {
        return _appResource ?: (_appResource = AppUtil.getBean(AppResourceTagLib))
    }

    String getTenantId() {
        if(cloudConfig && cloudConfig.baseUrl){
            return ""
        }else{
            return TenantContext.currentTenant + "/"
        }
    }

    String getBaseUrl(){
        if(cloudConfig && cloudConfig.baseUrl){
            return cloudConfig.baseUrl
        }else{
            return app.relativeBaseUrl()
        }
    }

    abstract void setBaseUrl(String baseUrl)
    abstract String getResourceName()
    abstract void setResourceName(String resourceName)
    abstract String getRelativeUrl()

    static transients = ['resourceName', 'relativeUrl','setUrlInfix']

    String getRelativePath() {
        return "${PathManager.resourceURLRoot}${relativeUrl}"
    }

    String getResourceRelativePath() {
        return "${relativeUrl}${resourceName}"
    }

    String getResourceRelativePath(String prefix) {
        return "${relativeUrl}${prefix}-${resourceName}"
    }

    String getResourceRelativeUrl() {
        return "resources/${getResourceRelativePath()}"
    }

    String getResourceRelativeUrl(String resourceNamePrefix) {
        return "resources/${getResourceRelativePath(resourceNamePrefix)}"
    }

    String getResourceUrl() {
        if (baseUrl) {
            return "${baseUrl}${getResourceRelativeUrl()}"
        }
        return "${app.relativeBaseUrl()}${getRelativePath()}${resourceName}"
    }

    String getResourceUrl(String resourceNamePrefix) {
        if(baseUrl) {
            return "${baseUrl}${getResourceRelativeUrl(resourceNamePrefix)}"
        }
        return "${app.relativeBaseUrl()}${getRelativePath()}${resourceNamePrefix}-${resourceName}"
    }

    List<String> getPrefixes() {
        return  []
    }

    void removeResource() {
        FileUtil.deleteQuietly(new File(PathManager.getResourceRoot(resourceRelativePath)))
        if(getBaseUrl()) {
            CloudStorageManager.deleteData(resourceRelativeUrl, NamedConstants.CLOUD_CONFIG.DEFAULT)
        }
        prefixes.each {
            FileUtil.deleteQuietly(new File(PathManager.getResourceRoot(getResourceRelativePath(it))))
            if(getBaseUrl()) {
                CloudStorageManager.deleteData(getResourceRelativeUrl(it), NamedConstants.CLOUD_CONFIG.DEFAULT)
            }
        }
        AppEventManager.fire("after-remove-resource", [this])
    }

    def afterDelete() {
        removeResource()
    }
}

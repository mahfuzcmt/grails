package com.webcommander.models

import com.webcommander.manager.CloudStorageManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.CloudConfig

class TemplateData {

    List<Map> layouts
    List<Map> pages
    List<Map> resolutions
    Map<String, Map> widgetContents
    List<String> resources
    Map<String, Map> siteConfigs
    Map<String, String> autoPageMapping
    Map otherContents
    String sourceTenantId = TenantContext.currentTenant
    List<String> cloudUrls = []

    TemplateData() {
        layouts = new ArrayList<Map>();
        pages = new ArrayList<Map>();
        resolutions = new ArrayList<Map>();
        widgetContents = new HashMap<String, Map>()
        resources = new ArrayList<String>()
        siteConfigs = new HashMap<String, Map>()
        autoPageMapping = new HashMap<String, String>()
        cloudUrls = getCloudUrlList()
        otherContents = [:]
    }

    TemplateData(Map data) {
        this.layouts = data.layouts
        this.pages = data.pages
        this.resolutions = data.resolutions
        this.widgetContents = data.widgetContents
        this.siteConfigs = data.siteConfigs
        this.autoPageMapping = data.autoPageMapping
        this.otherContents = data.otherContents
        this.sourceTenantId = data.sourceTenantId ?: null
        this.cloudUrls =  data.cloudUrls ?:[]
    }


    List<String> getCloudUrlList() {
        List<CloudConfig> cloudConfigList = CloudStorageManager.getAllCloudConfig()
        def urls = []
        if (cloudConfigList) {
            cloudConfigList.each { config ->
                urls.add(config.baseUrl)
            }
        }
        return urls
    }


    Map toMap() {
        Map map = this.properties
        map.remove("resources")
        return map
    }

    List getWidgetContents(String contentType) {
        return widgetContents[contentType] ?: []
}

    Map getWidgetContent(String contentType, def contentId) {
        return widgetContents[contentType]?.find { it.id == contentId}
    }

    List getOtherContents(String contentType) {
        return otherContents[contentType] ?: []
    }

    void collectSiteConfig(String type, String key = null) {
        if(!this.siteConfigs.containsKey(type)) {
            this.siteConfigs[type] = new HashMap()
        }
        Map config
        if(key) {
            config = [(key): AppUtil.getConfig(type, key)]
        } else {
            config = AppUtil.getConfig(type)
        }
        this.siteConfigs[type] << config
    }
}

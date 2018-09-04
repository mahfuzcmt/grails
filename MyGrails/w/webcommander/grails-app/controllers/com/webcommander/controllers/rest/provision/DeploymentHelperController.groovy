package com.webcommander.controllers.rest.provision

import com.webcommander.constants.DomainConstants
import com.webcommander.converter.json.JSON
import com.webcommander.manager.CacheManager
import com.webcommander.plugin.PluginManager
import com.webcommander.provision.DeploymentHelperService
import com.webcommander.task.OfflineQueryQueueService
import com.webcommander.tenant.TenantPropsResolver
import com.webcommander.util.RestProcessor

class DeploymentHelperController extends RestProcessor {

    DeploymentHelperService deploymentHelperService
    OfflineQueryQueueService offlineQueryQueueService
    def sessionFactory


    def getInstalledPlugins() {
        rest((PluginManager.getActivatedPluginList() as JSON).toString())
    }


    def installPlugins() {
        deploymentHelperService.installPlugins(params.list("names"))
        render("Installation Process Started.")
    }

    def installPluginsAndTemplate() {
        def operator = deploymentHelperService.getDeploymentOperator(params)
        if (operator) {
            request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.ADMIN, operator.id)
        }
        request.setAttribute(DomainConstants.REQUEST_ATTR_KEYS.IS_API_REQUEST, true)
        deploymentHelperService.installPluginAndTemplate(request.JSON)
        render("Installation Process Started.")
    }


    def reloadTenantCaches() {
        try {
            TenantPropsResolver.reload()
        }catch (Exception e){
            e.printStackTrace()
            println(e.getMessage())
        }
        render "Reloaded"
    }


    def runOfflineQuery() {
        offlineQueryQueueService.performQueries()
        render "Executed"
    }

    def cleanCacheManager() {
        sessionFactory.queryCache.clear()
        sessionFactory.cache.evictAllRegions()
        CacheManager.clearAll()
        render "Clean All Cache"
    }

}

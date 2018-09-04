package com.webcommander.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.CacheManager
import com.webcommander.plugin.PluginMeta
import com.webcommander.plugin.PluginManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

class PluginService {
    ProvisionAPIService provisionAPIService
    def sessionFactory


    def getInstallablePlugin() {
        Boolean isSoftInstallUnInstall = PluginManager.isSoftInstallUninstallEnable()
        // TODO: After Provision Work done will back here
//        Boolean isProvisioningEnable = Holders.config.webcommander.provision.enabled
        Boolean isProvisioningEnable = false
        Map licenseConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE)
        def plugins = isProvisioningEnable ? provisionAPIService.getPluginsByLicense() : [plugin: PluginManager.loadedPlugins - PluginManager.getActivePlugins(), installed: PluginManager.getActivePlugins()]
        return [plugins: plugins, licenseConfig: licenseConfig]
    }

    Map getPlugins(Map params) {
        List<PluginMeta> loadedPlugins = filterByDate(PluginManager.activePlugins, params)
        loadedPlugins = filterByName(loadedPlugins, params)
        int size = loadedPlugins.size()
        int max = (params.max).toInteger()
        if(max < 0) {
            max = size
        }
        int offset = (params.offset).toInteger()
        int start = offset > size  ? size : offset
        int end = ((start + max) > size) ? size : (start + max)
        boolean asc = params.dir != "desc"
        loadedPlugins.sort { a, b ->
            asc ? a.name <=> b.name : b.name <=> a.name
        }
        loadedPlugins = loadedPlugins.subList(start, end)
        return [loadedPlugins: loadedPlugins, count: size]
    }

    private List filterByName(List pluginsList, Map params) {
        if(params.searchText) {
            String text = (params.searchText).toLowerCase()
            pluginsList = []
            PluginManager.activePlugins.each {
                if(it.name.toLowerCase().matches(".*" + text + ".*")) {
                    pluginsList.add(it)
                }
            }
            return pluginsList
        }
        return pluginsList
    }

    private List filterByDate(List pluginsList, Map params) {
        if(params.pluginFrom == null && params.pluginTo == null) {
            return pluginsList
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")
        String from = params.pluginFrom
        String to = params.pluginTo
        Date pluginFrom = from? formatter.parse(from) : formatter.parse("0001-00-00")
        Date pluginTo = to? formatter.parse(to) : formatter.parse("9999-12-31")
        pluginsList = []
        PluginManager.activePlugins.each {
            Date installed = it.installed?: formatter.parse("0001-00-00")
            if(installed >= pluginFrom && installed <= pluginTo) {
                pluginsList.add(it)
            }
        }
        return pluginsList
    }

    @Transactional
    Boolean activate(identifier) {
        synchronized (TenantContext.currentTenant) {
            if(PluginManager.isInstalled(identifier)) {
                throw new ApplicationRuntimeException("$identifier already installed")
            }
            try {
                PluginManager.activatePlugin(identifier)
                sessionFactory.queryCache.clear()
                sessionFactory.cache.evictAllRegions()
                CacheManager.clearAll()
                return true
            } catch (Throwable e) {
                log.error(e.message, e)
                return  false
            }
        }
    }

    @Transactional
    Boolean deActivate(identifier) {
        synchronized (TenantContext.currentTenant) {
            if(!PluginManager.isInstalled(identifier)) {
                throw new ApplicationRuntimeException("x.not.installed", [identifier])
            }
            try {
                PluginMeta meta = PluginManager.activePlugins.find { it.identifier == identifier }
                // TODO: Need to address
//                if(meta && meta.dependents.size()) {
//                    meta.dependents.each { depend ->
//                        if(PluginManager.isInstalled(depend)) {
//                            PluginManager.deActivatePlugin(depend)
//                        }
//                    }
//
//                }
                PluginManager.deActivatePlugin(identifier)
                CacheManager.clearAll()
                sessionFactory.queryCache.clear()
                sessionFactory.cache.evictAllRegions()
                return true
            } catch (Throwable e) {
                log.error(e.message, e)
                return  false
            }
        }
    }

    @Transactional(noRollbackFor = Exception.class)
    synchronized Boolean install(Map params) {
        String identifier = params.identifier
        if(PluginManager.isInstalled(identifier)) {
            try {
                provisionAPIService.actionFeedBack('No', "Attempt to installed Plugin '${params.name}' but found as installed")
            } catch (Throwable ignored) {
                log.error(ignored.message)
            }
            throw new ApplicationRuntimeException("$params.name already installed")
        }
        try {
            PluginMeta meta = PluginManager.loadedPlugins.find { it.identifier == identifier }
            if(meta && meta.dependents.size()) {
                meta.dependents.each { depend ->
                    if(!PluginManager.isInstalled(depend)) {
                        throw new ApplicationRuntimeException("dependent.plugin.x.not.installed", [depend])
                    }
                }
            }
            if(PluginManager.isSoftInstallUninstallEnable()) {
                activate(identifier)
            } else {
                //TODO: have to implement installation for single tenant
            }
            return true
        } catch (Throwable e) {
            log.error(e.message, e)
            try {
                provisionAPIService.actionFeedBack('No', "Plugin '${identifier}' couldn't be installed")
            } catch (Throwable ignored) {
                log.error(ignored.message)
            }
            if(e instanceof ApplicationRuntimeException) {
                throw e
            }
            return  false
        }
    }

    void uninstall(String identifier, List dependent = []) {
        File file = new File(Holders.servletContext.getRealPath("/WEB-INF/to-uninstall.xml"))
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        xml.plugins {
            if (file.exists()) {
                def existingList = new XmlParser().parse(file)
                existingList.plugin.each {
                    plugin(it.text())
                }
            }
            dependent.each {
                plugin(it)
            }
            plugin(identifier)
        }
        file.write(writer.toString())
    }
}

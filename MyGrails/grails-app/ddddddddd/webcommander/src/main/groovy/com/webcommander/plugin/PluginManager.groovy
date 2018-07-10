package com.webcommander.plugin

import com.webcommander.config.ActivePlugin
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.LicenseManager
import com.webcommander.manager.PathManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.tenant.TenantContext
import com.webcommander.util.PluginDestroyUtil
import grails.converters.JSON
import grails.util.Environment
import grails.util.Holders
import groovy.util.logging.Log
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.taglib.TagLibraryLookup
import org.grails.web.servlet.boostrap.BootstrapArtefactHandler
import org.springframework.context.ApplicationContext
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

@Log
class PluginManager {
    static List<PluginMeta> loadedPlugins = []
    private static TagLibraryLookup _gspTagLibraryLookup
    private static _g
    String PLUGIN_JAR_FILE_NAME = "plugin.jar"
    String PLUGIN_INFO_FILE_NAME = "info.json"
    String PLUGIN_SCAN_LOCATION = "WEB-INF/install-plugins"
    String PLUGIN_BINARY_EXTENSION = ".zip"
    String INSTALLED_PLUGIN_LOCATION = "WEB-INF/wc-plugins"

    ProvisionAPIService provisionAPIService

    PluginManager() {
        provisionAPIService = new ProvisionAPIService()
    }

    private static TagLibraryLookup getGspTagLibraryLookup() {
        return  _gspTagLibraryLookup ?: (_gspTagLibraryLookup = Holders.grailsApplication.mainContext.getBean("gspTagLibraryLookup"))
    }

    private static ApplicationTagLib getG() {
            return _g ?: (_g = Holders.grailsApplication.mainContext.getBean("org.grails.plugins.web.taglib.ApplicationTagLib"))
    }

    static boolean isSoftInstallUninstallEnable() {
        return Holders.config.webcommander.plugin.softInstallUnInstall
    }

    /**
     * @param place
     * @param attrs
     * @param body closure must return a string
     * @param out
     * @return
     */
    static Object hookTag(String place, Map attrs, Closure body, Writer out) {
        boolean no_hook = true
        activePlugins.each { _plugin ->
            PluginMeta.HookPoint hook = _plugin.hookPoints[place]
            String requiredLicense
            if (hook && (!(requiredLicense = hook.license ?: _plugin.requiredLicense) || LicenseManager.isAllowed(requiredLicense) || requiredLicense == "false")) {
                no_hook = false
                if (hook.taglib) {
                    def tagLib = gspTagLibraryLookup.lookupTagLibrary(hook.taglib, hook.callable)
                    if (tagLib) {
                        Closure _body = body
                        body = {
                            return tagLib."$hook.callable"(attrs, _body)
                        }
                    }
                } else if (hook.view) {
                    out << g.include(view: hook.view, params: attrs)
                }
            }
        }
        if (body) {
            out << body()
        }
    }

    def getPluginInfo(ZipFile zipFile) {
        if (zipFile.getEntry(PLUGIN_INFO_FILE_NAME) != null) {
            String json = zipFile.getInputStream(zipFile.getEntry(PLUGIN_INFO_FILE_NAME)).text
            return JSON.parse(json)
        }
        return null
    }

    def getPluginInfoByIdentifier(String identifier) {
        String location = PathManager.getRoot(INSTALLED_PLUGIN_LOCATION + "/" + identifier + "/$PLUGIN_INFO_FILE_NAME")
        File pluginInfo = new File(location)
        if (pluginInfo.exists()) {
            return JSON.parse(pluginInfo.text)
        }
        return null
    }

    def static pluginLog(String logMessage, String logType = "severe") {
        log."${logType}"(logMessage)
    }

    static List<PluginMeta> getActivePlugins() {
        List<PluginMeta> activePlugins = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "plugin", "active")
        if(!activePlugins) {
            activePlugins = new Vector<PluginMeta>(ActivePlugin.where {}.property("name").collect { name -> loadedPlugins.find {it == name}})
            activePlugins.removeAll { !it }
            CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, activePlugins, "plugin", "active")
        }
        return activePlugins
    }

    static boolean isInstalled(String identifier) {
        activePlugins.find { plugin ->
            plugin == identifier
        } != null
    }

    static boolean isLoaded(String identifier) {
        loadedPlugins.find { plugin ->
            plugin == identifier
        } != null
    }

    def scanAndInstall(ApplicationContext applicationContext) {
        File file = new File(PathManager.getRoot(PLUGIN_SCAN_LOCATION))
        if (file.exists()) {
            file.listFiles().each { plugin ->
                if (plugin.name.lastIndexOf(".") > 0) {
                    String extension = plugin.name[plugin.name.lastIndexOf('.') ..< plugin.name.size()]
                    if (extension.equals(PLUGIN_BINARY_EXTENSION)) {
                        ZipFile zipFile = new ZipFile(plugin)
                        if (validatePluginZip(zipFile)) {
                            def pluginInfo = getPluginInfo(zipFile)
                            if (pluginInfo != null && pluginInfo.identifier) {
                                if (isLoaded(pluginInfo.identifier)) {
                                    pluginLog("Plugin " + plugin.name + " already installed.", "info")
                                } else {
                                    pluginLog("Installing " + plugin.name + " Plugin binary.", "info")
                                    boolean installed = false
                                    plugin.withInputStream { stream ->
                                        if((installed = install(applicationContext.environment.getProperty("webcommander.version.number"), pluginInfo.identifier, stream))) {
                                            pluginLog("Installed " + plugin.name + " Plugin binary.", "info")
                                        } else {
                                            pluginLog("Unable to Install " + plugin.name + " Plugin binary.",)
                                        }
                                    }
                                    if(installed) {
                                        plugin.delete()
                                    }
                                }
                            } else {
                                pluginLog("Invalid plugin info.")
                            }
                        } else {
                        }
                    }
                }
            }
        }
    }

    private validatePluginZip(ZipFile zipFile) {
        if (zipFile.getEntry(PLUGIN_INFO_FILE_NAME) != null && zipFile.getEntry(PLUGIN_JAR_FILE_NAME) != null) {
            return true
        }
        return false
    }

    def install(String appVersion, String identifier, InputStream pluginInputStream) {
        ZipInputStream stream
        String root = PathManager.root
        try {
            stream = new ZipInputStream(pluginInputStream)
            ZipEntry zipEntry = stream.nextEntry
            while (zipEntry) {
                if (!zipEntry.isDirectory()) {
                    String entryName = zipEntry.name
                    String outFilePath
                    if (entryName.startsWith("public/")) {
                        outFilePath = root + "wc/${appVersion}/plugins/" + identifier + "/" + entryName.substring(7)
                    } else if (entryName.startsWith("WEB-INF/system-resources/")) {
                        outFilePath = root + entryName
                    } else {
                        outFilePath = root + "WEB-INF/wc-plugins/" + identifier + "/" + entryName
                    }
                    File outFile = new File(outFilePath)
                    if (!outFile.parentFile.exists()) {
                        outFile.parentFile.mkdirs()
                    }
                    outFile.withOutputStream { outputStream ->
                        outputStream << stream
                    }
                    stream.closeEntry()
                }
                zipEntry = stream.nextEntry
            }
            return true
        } catch (Throwable e) {
            new File(root + "WEB-INF/wc-plugins/" + identifier).deleteDir()
            pluginLog("From Plugin install: " + e.getMessage())
            return false
        } finally {
            if(stream) {
                stream.closeEntry()
                stream.close()
            }
        }
    }

    private jarLoader(File file, ApplicationContext applicationContext) {
        file.listFiles().each { pluginFile ->
            String extension = pluginFile.name[pluginFile.name.lastIndexOf('.')..<pluginFile.name.size()]
            if (extension.equals(".jar")) {
                applicationContext.getClassLoader().addURL(pluginFile.toURI().toURL())
            }
        }
    }

    def registerPlugins(ApplicationContext applicationContext) {
        File file = new File(PathManager.getRoot("WEB-INF/wc-plugins"))
        if (file.exists()) {
            file.listFiles().each { plugin ->
                def jarDir = new File(plugin, "lib")
                if (jarDir.exists()) {
                    jarLoader(jarDir, applicationContext)
                }
                jarLoader(plugin, applicationContext)
            }
        }
    }

    def uninstall() {
    }

    static String getPluginName(Class clazz) {
        String pluginId
        clazz.name.eachMatch(/^com\.webcommander\.plugin\.([^\.]+)\..+/) {
            pluginId = it[1]
        }
        return pluginId?.replace("_", "-")
    }

    private void uninstallPlugin(String identifier) {
        String rootPath = PathManager.getRoot()
        File destroyerSource = new File(rootPath + "WEB-INF/wc-plugins/${identifier}/PluginDestroyer.groovy")
        if (destroyerSource.exists()) {
            GroovyClassLoader classLoader = new GroovyClassLoader(PluginDestroyUtil.classLoader)
            Class destroyerClass = classLoader.parseClass(destroyerSource)
            def destroyer = destroyerClass.newInstance()
            try {
                destroyer.destroy()
            } catch (Exception e) {
                pluginLog(e.message)
            }
        }

        File publicFiles = new File(rootPath + "plugins/" + identifier)
        if (publicFiles.exists()) {
            publicFiles.deleteDir()
        }

        def pluginInfoJson = getPluginInfoByIdentifier(identifier)
        if (pluginInfoJson != null) {
            pluginInfoJson.resources?.each {
                File systemFile = new File(rootPath + "WEB-INF/system-resources" + it)
                if (systemFile.exists()) {
                    if (systemFile.isDirectory()) {
                        if (systemFile.list().length == 0) {
                            systemFile.delete()
                        }
                    } else {
                        systemFile.delete()
                    }
                }
            }
        }

        File pluginsFile = new File(rootPath + "WEB-INF/wc-plugins/" + identifier)
        if (pluginsFile.exists()) {
            pluginsFile.deleteDir()
        }

        pluginLog("${identifier} plugin has been uninstalled", "info")
        AppEventManager.one("wc-startup", {
            try {
                provisionAPIService.pluginInstalledUninstalled(identifier, "false")
                provisionAPIService.actionFeedBack("success", "${identifier} Plugin has been uninstalled successfully")
            } catch (Throwable ignored) {
                pluginLog(ignored.message, "info")
            }
        })
    }

    //TODO: have to use it on uninstall
    static void uninstallPlugins() {
        File file = new File(Holders.servletContext.getRealPath("WEB-INF/to-uninstall.xml"))
        if (file.exists()) {
            def plugins = new XmlParser().parse(file)
            plugins.plugin.each {
                uninstallPlugin(it.text())
            }
            file.delete()
        }
    }

    static def activateLoadedPlugin() {
        if ((ActivePlugin.count() == 0 || Environment.current.is(Environment.DEVELOPMENT)) && !TenantContext.isMultiTenantEnabled()){
            def loadedPluginMap = [:]
            loadedPlugins.each { plugin ->
                loadedPluginMap.put(plugin.identifier, plugin)
                if (!Holders.config.webcommander.provision.enabled) {
                    if (!ActivePlugin.findByName(plugin.identifier)){
                        new ActivePlugin(name: plugin.identifier).save(flush: true)
                    }
                }
            }
            if (Holders.config.webcommander.provision.enabled) {
                ProvisionAPIService proviAPIService = new ProvisionAPIService()
                def packagePlugins = proviAPIService.getPluginsByLicense()
                if (packagePlugins) {
                    packagePlugins.each { plugin ->
                        if (loadedPluginMap.get(plugin.identifier) != null) {
                            new ActivePlugin(name: plugin.identifier).save(flush: true)
                        }
                    }
                }
            }
        }
    }

    static def activatePluginFromList(List<String> list){
        list.each { plugin ->
            activatePlugin(plugin)
        }
    }

    static List getActivatedPluginList(){
        List pluginList = []
        ActivePlugin.list().each {
            pluginList.add(it.name)
        }
        return pluginList
    }

    static def activatePlugin(String identifier) {
        PluginMeta plugin = loadedPlugins.find { plugin ->
            plugin == identifier
        }

        if(!plugin || isInstalled(identifier)){
            return
        }

        activePlugins.add(plugin)
        new ActivePlugin(name: identifier).save()

        PluginActivator.buildInitSchema(identifier)
        String packageName = "com.webcommander.plugin.${plugin.identifier.replace('-', '_')}"
        def bootStrap = Holders.grailsApplication.getArtefact(BootstrapArtefactHandler.TYPE, "${packageName}.BootStrap")?.referenceInstance
        if(bootStrap) {
            Closure init = ClassPropertyFetcher.getInstancePropertyValue(bootStrap, "tenantInit")
            if(init) {
                init TenantContext.currentTenant
            }
        }

        Collection<PersistentEntity> entities = Holders.grailsApplication.mappingContext.persistentEntities
        entities.each { entity ->
            if (entity.javaClass.package.name.contains(packageName)){
                try{
                    entity.javaClass.initialize()
                }catch (Exception e){}
            }
        }

//        PluginActivator.updateMinifiedJs()
//        PluginActivator.updateMinifiedCss()
//        PluginActivator.updateMergedJsMessageFile()
    }

    static def deActivatePlugin(String identifier) {
        PluginMeta plugin = loadedPlugins.find { plugin ->
            plugin == identifier
        }
       if (plugin){
           getActivePlugins().remove(plugin)
           ActivePlugin.findByName(identifier).delete()
           def bootStrap = Holders.grailsApplication.getArtefact(BootstrapArtefactHandler.TYPE, "com.webcommander.plugin.${plugin.identifier.replace('-', '_')}.BootStrap")?.referenceInstance
           if(bootStrap) {
               Closure destroy = ClassPropertyFetcher.getInstancePropertyValue(bootStrap, "tenantDestroy")
               if(destroy) {
                   destroy TenantContext.currentTenant
               }
           }
           PluginActivator.removeSchema(identifier)
       }
//        PluginActivator.updateMinifiedJs()
//        PluginActivator.updateMinifiedCss()
//        PluginActivator.updateMergedJsMessageFile()
    }
}
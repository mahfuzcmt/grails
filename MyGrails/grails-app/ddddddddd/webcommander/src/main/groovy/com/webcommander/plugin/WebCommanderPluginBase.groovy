package com.webcommander.plugin

import com.webcommander.manager.PathManager
import grails.plugins.Plugin
import groovy.io.FileType
import groovy.io.FileVisitResult
import org.apache.commons.io.FileUtils
import grails.util.Environment

/**
 * Created by zobair on 22/02/14.*/

class WebCommanderPluginBase extends Plugin {
    def watchedResources = ["file:./src/main/webapp/*.js", "file:./src/main/webapp/js/*.properties", "file:./src/main/webapp/*.css", "file:./src/main/webapp/images/**"]
    String pluginResourceBase
    PluginMeta _plugin
    def hooks = []
    def depends = []

    def dependsOn = [webcommander: "* > 0.0.0"]
    
    void doWithApplicationContext() {
        if(depends) {
            _plugin.dependents.addAll(depends)
        }
        PluginManager.loadedPlugins.add(_plugin)
        if (Environment.current.is(Environment.DEVELOPMENT)) {
            pluginResourceBase = PathManager.getPluginSystemResourceRoot _plugin
            new File(PathManager.getSystemResourceRoot("plugins/$_plugin.identifier")).deleteDir()
            File resourceBase = new File(pluginResourceBase)
            if (resourceBase.exists()) {
                resourceBase.traverse([type: FileType.FILES, preDir: {
                    if (it.name == "WEB-INF" || it.name == "META-INF") {
                        return FileVisitResult.SKIP_SUBTREE
                    }
                }]) { _file ->
                    updateResource(_file)
                }
            }
            File resourceFolder = new File(PathManager.getPluginRestrictedResourceRoot(_plugin))
            File destinationFolder = new File(PathManager.getRestrictedResourceRoot())
            if (resourceFolder.exists()) {
                if (!destinationFolder.exists()) {
                    destinationFolder.mkdirs()
                }
                FileUtils.copyDirectory(resourceFolder, destinationFolder)
            }
        }
        _plugin.updatePluginResourceMeta()

        hooks.each { hook ->
            _plugin.hookPoints.put(hook.key, new PluginMeta.HookPoint(hook.value))
        }
        _plugin.registerHooks()
    }
    
    void onChange(Map<String, Object> event) {
        File changeFile = event.source.file
        if(changeFile.absolutePath.startsWith(pluginResourceBase.replace("/", File.separator))) {
            updateResource(changeFile)
        }
    }
    
    def updateResource(File file) {
        File resourceBase = new File(PathManager.getSystemResourceRoot("plugins/$_plugin.identifier"))
        resourceBase.mkdirs()
        String suffix = file.absolutePath.substring(pluginResourceBase.length())
        File out = new File(resourceBase, suffix)
        out.parentFile.mkdirs()
        file.withInputStream { _in ->
            out.withOutputStream { _out ->
                _out << _in
            }
        }
    }
}
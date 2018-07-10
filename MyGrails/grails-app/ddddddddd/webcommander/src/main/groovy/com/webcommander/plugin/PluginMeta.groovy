package com.webcommander.plugin

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import grails.util.Holders
import groovy.io.FileType

import java.lang.reflect.Method

class PluginMeta {
    static class HookPoint {
        String bean
        String callable
        String taglib
        String clazz
        String view
        String license // for "false" value will not check license
    }

    Map<String, HookPoint> hookPoints = [:]
    String name
    String description
    String identifier
    String version
    String requiredLicense
    String pluginType = NamedConstants.WC_BEHAVIOUR_TYPE.CONTENT
    ClassLoader loader
    Date installed

    Map autoPageJS = [:]
    Map autoPageCSS = [:]
    List<String> siteJS = []
    List<String> dependents = []

    void updatePluginResourceMeta() {
        updatePluginSiteJS()
        updatePluginAutoPageJS()
        updatePluginAutoPageCSS()
    }

    private updatePluginSiteJS() {
        String jsPath = PathManager.getSystemResourceRoot("plugins/$identifier/js/site")
        File file = new File(jsPath)
        int cutLength = file.toURI().path.length()
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                siteJS.add("js/site/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    private updatePluginAutoPageJS() {
        String jsPath = PathManager.getSystemResourceRoot("plugins/$identifier/js/auto_page")
        File file = new File(jsPath)
        int cutLength = file.toURI().path.length()
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                String name = _file.name
                name = name.substring(0, name.length() - 3)
                autoPageJS.put(name, "plugins/$identifier/js/auto_page/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    private updatePluginAutoPageCSS() {
        String cssPath = PathManager.getSystemResourceRoot("plugins/$identifier/css/auto_page")
        File file = new File(cssPath)
        int cutLength = file.toURI().path.length()
        if(file.exists()) {
            file.traverse([type: FileType.FILES]) { _file ->
                String name = _file.name
                name = name.substring(0, name.length() - 4)
                autoPageCSS.put(name, "plugins/$identifier/css/auto_page/" + _file.toURI().path.substring(cutLength))
            }
        }
    }

    def registerHooks() {
        hookPoints.each { name, hookpoint ->
            if(hookpoint.bean) {
                HookManager.register name, identifier, { param ->
                    def bean = Holders.applicationContext.getBean(hookpoint.bean)
                    Method method
                    if(bean) {
                        method = bean.class.getDeclaredMethods().find { lookupmethod ->
                            lookupmethod.name == hookpoint.callable
                        }
                        if(method) {
                            return method.invoke(bean, param.toArray(Object[]))
                        }
                    }
                    return null
                }
            }
        }
    }

    @Override
    boolean equals(Object obj) {
        return obj instanceof String ? identifier == obj : super.equals(obj)
    }
}
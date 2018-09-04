package com.webcommander

import com.webcommander.manager.PathManager
import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CacheManager
import com.webcommander.plugin.PluginManager
import com.webcommander.tenant.TenantContext
import grails.util.Environment
import groovy.io.FileType

class PluginTagLib {
    static namespace = "plugin"

    def each = { attr, body ->
        def page = this
        def cache_plugin = this.pageScope.plugin
        def cache_status = attr.status ? this.pageScope[attr.status] : null
        int status = 0
        List filteredPlugins = PluginManager.activePlugins.findAll { plugin ->
            if(attr.filter) {
                if(!attr.filter(plugin)) {
                    return false
                }
            }
            return true
        }
        def count_cache = page.pageScope.count
        page.pageScope.count = filteredPlugins.size()
        filteredPlugins.each { plugin ->
            page.pageScope.plugin = plugin
            if(attr.status) {
                page.pageScope[attr.status] = status++
            }
            out << body()
        }
        page.pageScope.count = count_cache
        this.pageScope.plugin = cache_plugin
        if(attr.status) {
            this.pageScope[attr.status] = cache_status
        }
    }

    def siteJSs = { attrs, body ->
        String hash = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "activePluginsHash")
        //TODO: blocked as  generated file uploading to cloud process not decided
        /*if(Environment.current == Environment.PRODUCTION) {
            out << app.javascript(src: "production-minified/$TenantContext.currentTenant/plugin-site-fixed.js?_=$hash")
        } else {*/
            PluginManager.activePlugins.each { plugin ->
                plugin.siteJS.each { js ->
                    out << app.javascript(src: "plugins/$plugin.identifier/$js")
                }
            }
        //}
    }

    def siteCSSs = { attrs, body ->
        String hash = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "activePluginsHash") ?: CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, Integer.toHexString(PluginManager.activePlugins.hashCode()), "activePluginsHash")
        //TODO: blocked as  generated file uploading to cloud process not decided
        /*if(Environment.current == Environment.PRODUCTION) {
            out << app.stylesheet(href: "production-minified/$TenantContext.currentTenant/plugin-site-fixed.css?_=$hash")
        } else {*/
            PluginManager.activePlugins.each { plugin ->
                String cssBasePath = PathManager.getSystemResourceRoot("plugins/$plugin.identifier/css/site")
                File file = new File(cssBasePath, "base.css")
                if(file.exists()) {
                    out << app.stylesheet(href: "plugins/$plugin.identifier/css/site/base.css")
                }
            }
        //}
    }

    def adminCSSs = { attrs, body ->
        String hash = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, "activePluginsHash") ?: CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, Integer.toHexString(PluginManager.activePlugins.hashCode()), "activePluginsHash")
        //TODO: blocked as  generated file uploading to cloud process not decided
        /*if(Environment.current == Environment.PRODUCTION) {
            out << app.stylesheet(href: "production-minified/$TenantContext.currentTenant/plugin-admin-fixed.css?_=$hash")
        } else {*/
            PluginManager.activePlugins.each { plugin ->
                String suffixBase = "plugins/$plugin.identifier/css/admin/"
                File cssBaseFile = new File(PathManager.getSystemResourceRoot(suffixBase))
                int cutBaseLength = cssBaseFile.toURL().toString().length()
                if (cssBaseFile.exists()) {
                    cssBaseFile.traverse([type: FileType.FILES]) { _file ->
                        String path = suffixBase + _file.toURL().toString().substring(cutBaseLength)
                        out << app.stylesheet(href: path)
                    }
                }
            }
        //}
        PluginManager.hookTag("admin-css", attrs, body, out)
    }

    def frondEndEditorJSs = {attrs, body ->
        def files = []
        PluginManager.activePlugins.each { plugin ->
            String jsPath = PathManager.getSystemResourceRoot("plugins/$plugin.identifier/js/front-end-editor")
            File file = new File(jsPath)
            int cutLength = file.toURI().path.length()
            if(file.exists()) {
                file.traverse([type: FileType.FILES]) { _file ->
                    files.add("plugins/$plugin.identifier/js/front-end-editor/" + _file.toURI().path.substring(cutLength))
                }
            }
        }
        files.each { _path ->
            out <<  app.javascript(src: _path)
        }
    }

    def frontEndEditorCSSs = { attrs, body ->
        PluginManager.activePlugins.each { plugin ->
            String path = "plugins/$plugin.identifier/css/front-end-editor/editor.css"
            File file = new File(PathManager.getSystemResourceRoot(path))
            if(file.exists()) {
                out << app.stylesheet(href: path)
            }
        }
    }

    def hookTag = { attrs, body ->
        PluginManager.hookTag(attrs.hookPoint, attrs.attrs, body, out)
    }

    def isInstalled = {attrs, body ->
        if(PluginManager.isInstalled(attrs.identifier)) {
            out << body()
        }
    }
}

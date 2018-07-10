package com.webcommander.plugin

import com.google.javascript.jscomp.CommandLineRunner
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.tenant.TenantPropsResolver
import com.webcommander.util.AppUtil
import com.yahoo.platform.yui.compressor.CssCompressor
import groovy.io.FileType
import groovy.sql.Sql
import org.apache.commons.io.FilenameUtils

class PluginActivator {

    static boolean schemaCreateRemove(String location){
        File schemaFile = new File(location)
        if(schemaFile.exists()) {
            // each line should have a full sql
            schemaFile.readLines().each {
                if(!it.trim()) {
                    return
                }
                Sql sql = new Sql(AppUtil.getBean("dataSource"))
                sql.execute(it)
            }
            return true
        }
        return true
    }

    static boolean removeSchema(String identifier) {
        String removeSchema = PathManager.getPluginRoot(identifier, "sql/remove-schema-mysql.sql")
        return schemaCreateRemove(removeSchema)
    }

    static boolean buildInitSchema(String identifier) {
        String createSchema = PathManager.getPluginRoot(identifier, "sql/create-schema-mysql.sql")
        return schemaCreateRemove(createSchema)
    }

    static boolean updateMinifiedJs() {
        String root = PathManager.getSystemResourceRoot()

        Closure jsCompress = { inJs, outFile ->
            File minifiedFile = new File(root, "production-minified/${TenantContext.currentTenant}/$outFile")
            minifiedFile.parentFile.mkdirs()
            if(minifiedFile.exists()) {
                minifiedFile.createNewFile()
            }
            minifiedFile.text = inJs
            new CommandLineRunner(["--warning_level", "QUIET", "--js", minifiedFile.absolutePath, "--js_output_file", minifiedFile.absolutePath] as String[]).doRun()
        }

        def jsFullText = new StringBuilder()
        PluginManager.activePlugins.each { plugin ->
            plugin.siteJS.each { js ->
                File file = new File(root + "plugins/$plugin.identifier/$js")
                jsFullText << "\n; " + file.text
            }
        }
        jsCompress jsFullText.toString(), "plugin-site-fixed.js"

        jsFullText = new StringBuilder()
        def categoryJs = { category ->
            PluginManager.activePlugins.each { plugin ->
                File file = new File( PathManager.getSystemResourceRoot("plugins/$plugin.identifier/js/$category"))
                if (file.exists()) {
                    file.traverse([type: FileType.FILES]) { _file ->
                        jsFullText << "\n; " + _file.text
                    }
                }
            }
        }
        categoryJs("features")
        categoryJs("editors")
        categoryJs("app-widgets")
        jsCompress jsFullText.toString(), "plugin-admin-fixed.js"
    }

    static boolean updateMinifiedCss() {
        String root = PathManager.getSystemResourceRoot()

        Closure cssCompress = { inCss, outFile ->
            CssCompressor compressor = new CssCompressor(new StringReader(inCss))
            def cssCompressed = new StringWriter()
            compressor.compress(cssCompressed, -1)
            File minifiedFile = new File(root, "production-minified/${TenantContext.currentTenant}/$outFile")
            minifiedFile.parentFile.mkdirs()
            if(!minifiedFile.exists()) {
                minifiedFile.createNewFile()
            }
            minifiedFile.text = cssCompressed.toString()
        }

        def cssFullText = new StringBuilder()
        PluginManager.activePlugins.each { plugin ->
            File file = new File(root + "plugins/$plugin.identifier/css/site", "base.css")
            if(file.exists()) {
                cssFullText << "\n" + file.text
            }
        }
        cssCompress cssFullText.toString(), "plugin-site-fixed.css"

        cssFullText = new StringBuilder()
        PluginManager.activePlugins.each { plugin ->
            String suffixBase = "plugins/$plugin.identifier/css/admin/"
            File cssBaseFile = new File(root, suffixBase)
            if(cssBaseFile.exists()) {
                cssBaseFile.traverse([type: FileType.FILES]) { _file ->
                    cssFullText << _file.text
                }
            }
        }
        cssCompress cssFullText.toString(), "plugin-admin-fixed.css"
    }

    static boolean updateMergedJsMessageFile() {
        Map<String, Properties> localPropMap = [:]
        PluginManager.activePlugins.each { plugin ->
            File file = new File( PathManager.getSystemResourceRoot("plugins/$plugin.identifier/js/i18n"))
            if (file.exists()) {
                file.traverse([type: FileType.FILES]) { _file ->
                    String fileName = FilenameUtils.removeExtension _file.name
                    Properties prop = localPropMap[fileName] ?: (localPropMap[fileName] = new Properties())
                    _file.withInputStream {
                        prop.load(it)
                    }
                }
            }
        }
        localPropMap.each {String key, Properties file ->
            File mergedFile = new File( PathManager.getSystemResourceRoot("production-minified/${TenantContext.currentTenant}/${key}.properties"))
            if(!mergedFile.parentFile.exists()) {
                mergedFile.parentFile.mkdirs()
            }
            if(!mergedFile.exists()) {
                mergedFile.createNewFile()
            }
            mergedFile.withOutputStream {
                file.store(it, "")
            }
        }
    }
}
package com.webcommander.starter

import com.webcommander.plugin.PluginManager
import grails.boot.GrailsApp
import grails.boot.config.GrailsApplicationPostProcessor
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan("com.webcommander")
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Bean
    GrailsApplicationPostProcessor grailsApplicationPostProcessor() {
        PluginManager webCommanderPluginManager = new PluginManager()
        webCommanderPluginManager.scanAndInstall(applicationContext)
        webCommanderPluginManager.registerPlugins(applicationContext)
        return new GrailsApplicationPostProcessor(this, applicationContext, classes() as Class[])
    }
}
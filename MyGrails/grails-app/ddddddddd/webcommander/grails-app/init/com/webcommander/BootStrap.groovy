package com.webcommander

import com.google.javascript.jscomp.CommandLineRunner
import com.webcommander.admin.ConfigService
import com.webcommander.admin.LicenseService
import com.webcommander.annotations.Initializable
import com.webcommander.annotations.event.Event
import com.webcommander.annotations.event.EventHandler
import com.webcommander.authentication.ControllerAnnotationParser
import com.webcommander.config.SiteConfig
import com.webcommander.constants.ResourceList
import com.webcommander.events.AppEventManager
import com.webcommander.extension.grails.beans.LinkGenerator
import com.webcommander.extension.spring.DialectResolver
import com.webcommander.license.validator.ApiValidator
import com.webcommander.manager.CacheManager
import com.webcommander.manager.LicenseManager
import com.webcommander.manager.PathManager
import com.webcommander.plugin.PluginActivator
import com.webcommander.plugin.PluginManager
import com.webcommander.task.OfflineQueryQueueService
import com.webcommander.tenant.TenantContext
import com.webcommander.tenant.TenantPropsResolver
import com.webcommander.util.AppUtil
import com.webcommander.util.extenders.MetaClassUtil
import com.yahoo.platform.yui.compressor.CssCompressor
import grails.core.DefaultGrailsApplication
import grails.spring.BeanBuilder
import javassist.util.proxy.MethodFilter
import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.hibernate.dialect.Dialect
import org.hibernate.internal.SessionFactoryImpl

import java.lang.reflect.Field
import java.lang.reflect.Method

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

class BootStrap {

    DefaultGrailsApplication grailsApplication
    ConfigService configService
    OfflineQueryQueueService offlineQueryQueueService
    LicenseService licenseService
    static Integer initializeIndex = 1

    private void initializeEventHandlers() {
        grailsApplication.mainContext.getBeansWithAnnotation(EventHandler).values().each { bean ->
            bean.class.getDeclaredMethods().findAll {it.isAnnotationPresent(Event)}.each { handler ->
                Event event = handler.getAnnotation(Event)
                Closure handlerCaller = bean.&"${handler.name}"
                if(event.repetition() == "multiple" && event.value() != "application-start") {
                    AppEventManager.on event.value().join(" "), handlerCaller
                } else {
                    AppEventManager.one event.value().join(" "), handlerCaller
                }
            }
        }
    }

    private void initializeAnnotatedInitializers() {
        grailsApplication.mainContext.getBeansWithAnnotation(Initializable).values().each { bean ->
            bean.class.initialize()
        }
    }

    private void initializeServerUrlConfig() {
        grailsApplication.mainContext.removeBeanDefinition("grailsLinkGenerator")
        BeanBuilder bb = new BeanBuilder()
        bb.beans {
            grailsLinkGenerator(LinkGenerator, null)
        }
        bb.registerBeans(grailsApplication.mainContext)
    }

    private void initializeMultiDialect() {
        ProxyFactory dialectFactory = new ProxyFactory()
        dialectFactory.setSuperclass(Dialect)
        dialectFactory.setFilter(new MethodFilter() {
            @Override
            boolean isHandled(Method m) {
                return true
            }
        })
        Dialect dialect = dialectFactory.create([] as Class[], [] as Object[], new MethodHandler() {
            @Override
            Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                thisMethod.invoke(DialectResolver.currentDialect, *args)
            }
        })

        SessionFactoryImpl sessionFactory = AppUtil.getBean(org.hibernate.SessionFactory)
        Field f = SessionFactoryImpl.getDeclaredField("dialect")
        f.setAccessible(true)
        f.set(sessionFactory, dialect)
    }

    private void minifyCoreResources() {
        if(!new File(PathManager.getSystemResourceRoot("production-minified/site-fixed-core.js")).exists()) {
            String root = PathManager.getSystemResourceRoot()
            Closure cssCompress = { inFiles, outFile ->
                def cssFullText = new StringBuilder()
                inFiles.each { cssFullText << "\n" + new File(root, it).text }
                CssCompressor cssCompressor = new CssCompressor(new StringReader(cssFullText.toString()))
                def cssCompressed = new StringWriter()
                cssCompressor.compress(cssCompressed, -1)
                File minifiedFile = new File(root, "production-minified/$outFile")
                minifiedFile.parentFile.mkdirs()
                if(minifiedFile.exists()) {
                    minifiedFile.createNewFile()
                }
                minifiedFile.text = cssCompressed.toString()
            }

            cssCompress ResourceList.fixedSiteCss, "site-fixed.css"
            cssCompress ResourceList.fixedAdminCss, "admin-fixed.css"

            Closure jsCompress = { inFiles, outFile ->
                def jsFullText = new StringBuilder()
                inFiles.each { jsFullText << "\n; " + new File(root, it).text }
                File minifiedFile = new File(root, "production-minified/$outFile")
                minifiedFile.parentFile.mkdirs()
                if(minifiedFile.exists()) {
                    minifiedFile.createNewFile()
                }
                minifiedFile.text = jsFullText.toString()
                new CommandLineRunner(["--warning_level", "QUIET", "--js", minifiedFile.absolutePath, "--js_output_file", minifiedFile.absolutePath] as String[]).doRun()
            }

            jsCompress ResourceList.fixedSiteCoreJs, "site-fixed-core.js"
            jsCompress ResourceList.fixedSiteUIJs, "site-fixed-ui.js"
            jsCompress ResourceList.fixedAdminJs, "admin-fixed.js"
        }
    }


    def activatedPlugin(String tenantName){
        try {
            PluginManager.activateLoadedPlugin()
        }catch (Exception e){
            log.error("Tenant: ${tenantName}, Plugin Activation Error:" + e.getMessage())
        }
    }

    def activatedLicense(String tenantName){
        try {
            licenseService.refresh()
        }catch (Exception e){
            log.error("Tenant: ${tenantName}, License Activation Error:" + e.getMessage())
        }
    }

    def init = { servletContext ->
        CacheManager.removeAllCache()
        Collection<PersistentEntity> entities = grailsApplication.mappingContext.persistentEntities
        if (TenantContext.multiTenantEnabled) {
            //TODO: Happening problem when try to reload  widget service from tagLib
//            initializeServerUrlConfig()
            initializeMultiDialect()
            TenantPropsResolver.reload()
        }
        MetaClassUtil.init()
        initializeEventHandlers()
        initializeAnnotatedInitializers()
        AppEventManager.fire("application-start", [servletContext])
        ControllerAnnotationParser.init(grailsApplication.controllerClasses)

        minifyCoreResources()

        List<String> tenantsToRemove = []
        TenantContext.eachParallelWithWait { tenant, _session ->
            try {
                activatedPlugin(tenant)
                boolean initialized = TenantContext.multiTenantEnabled //&& AppUtil.getConfig(SITE_CONFIG_TYPES.GENERAL, "deployment_initialized")
                if(true) {
                    println("Initializing Virtual Instance ${initializeIndex} ${tenant}")
                    entities.each { entity ->
                        try {
                            Class domainClass = entity.javaClass
                            String plugin = PluginManager.getPluginName domainClass
                            if(!plugin || PluginManager.isInstalled(plugin)) {
                                domainClass.initialize()
                                _session.flush()
                            }
                        } catch (MissingMethodException e) {
                        }
                    }

                    AppEventManager.off("*", "bootstrap-init")

                    AppUtil.initializeDefaultImages(["product", "category", "store", "document"])

                    SiteConfig config = SiteConfig.where {
                        configKey == "deployment_initialized"
                    }.get()
                    if(config) {
                        config.value = "true"
                    } else {
                        config = new SiteConfig(type: SITE_CONFIG_TYPES.GENERAL, value: "true", configKey: "deployment_initialized")
                    }
                    config.save()
                    AppUtil.clearConfig(SITE_CONFIG_TYPES.GENERAL)
                    initializeIndex++
                }

                offlineQueryQueueService.performQueries()
                activatedLicense(tenant)


                if(new File(PathManager.getSystemResourceRoot("production-minified/${TenantContext.currentTenant}/plugin-site-fixed.js")).exists()) {
                    //TODO: Minify will deal next
//                    PluginActivator.updateMinifiedCss()
//                    PluginActivator.updateMinifiedJs()
//                    PluginActivator.updateMergedJsMessageFile()
                }
            } catch(Throwable h) {
                log.error "Could Not Start Tenant $tenant", h
                tenantsToRemove.add(tenant)
                initializeIndex++
            }
        }

        tenantsToRemove.each { tenant ->
            TenantPropsResolver.removeTenant(tenant)
        }

        servletContext.initialized = true
        AppEventManager.fire("wc-startup")

    }

    def destroy = {
    }
}

package com.webcommander.listener


class PluginAwareContextListener{}

//import com.webcommander.plugin.PluginAwareRootLoader
//import com.webcommander.spring.MultiConnectDataSource
//import com.webcommander.util.AppUtil
//import grails.util.Holders
//import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener
//import org.hibernate.cache.spi.CacheKey
//import org.hibernate.cache.spi.FilterKey
//import org.hibernate.cache.spi.NaturalIdCacheKey
//import org.hibernate.cache.spi.QueryKey
//import org.hibernate.engine.spi.CollectionKey
//import org.hibernate.engine.spi.EntityKey
//import org.hibernate.engine.spi.EntityUniqueKey
//import org.springframework.web.context.ConfigurableWebApplicationContext
//import org.springframework.web.context.WebApplicationContext
//import sun.misc.URLClassPath
//
//import javax.servlet.ServletContext
//import java.lang.reflect.Field
////import java.lang.reflect.Method
//
///**
// * Override listener class to have plugin class loader at initial domain collection class resolution
// */
//class PluginAwareContextListener extends GrailsContextLoaderListener {
//    @Override
//    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
//        super.configureAndRefreshWebApplicationContext(wac, sc)
//        Holders.grailsApplication.@classLoader = new PluginAwareRootLoader(Thread.currentThread().contextClassLoader)
//    }
//
//    @Override
//    WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
//        Object.metaClass.with {
//            getPrivateValue = { name, clazz = null ->
//                Field field = (clazz ?: delegate.class).getDeclaredField(name);
//                if(!field) {
//                    return null;
//                }
//                field.setAccessible(true);
//                return field.get(delegate);
//            }
//
//            setPrivateValue = { name, value, clazz = null ->
//                Field field = (clazz ?: delegate.class).getDeclaredField(name);
//                if(!field) {
//                    return;
//                }
//                field.setAccessible(true);
//                field.set delegate, value
//            }
//        }
////
////        URLClassLoader loader = this.class.classLoader;
////        int classesIndex = loader.URLs.findIndexOf {it.path.contains("classes")};
////        int coreIndex = loader.URLs.findIndexOf {it.path.contains("hibernate-core")};
////        if(coreIndex > -1) {
////            if(coreIndex < classesIndex) {
////                URLClassPath ucp = loader.getPrivateValue("ucp", URLClassLoader)
////                List<URL> urls = ucp.getPrivateValue("path", URLClassPath)
////                URL url = urls.remove(classesIndex)
////                urls.add(0, url);
////                Method method = URLClassPath.getDeclaredMethod("getLoader", URL);
////                method.setAccessible(true)
////                List loaders = ucp.getPrivateValue("loaders", URLClassPath)
////                Class loaderClass = URLClassPath.getDeclaredClasses().find {it.name == "sun.misc.URLClassPath\$Loader"}
////                int loaderIndex = loaders.findIndexOf {it.getPrivateValue("base", loaderClass).path.contains("classes")}
////                loaders.add(0, loaders.remove(loaderIndex));
////            }
////        } else {
////            URLClassLoader pLoader = loader.parent;
////            Field field = URLClassLoader.getDeclaredField("ucp");
////            field.setAccessible(true)
////            URLClassPath parentUcp = field.get(pLoader)
////            field = URLClassPath.getDeclaredField("path");
////            field.setAccessible(true)
////            List<URL> urls = field.get(parentUcp)
////            URL url = loader.URLs[classesIndex]
////            urls.add(0, url);
////            Method method = URLClassPath.getDeclaredMethod("getLoader", URL);
////            method.setAccessible(true)
////            def fLoader = method.invoke(parentUcp, url)
////            field = URLClassPath.getDeclaredField("loaders");
////            field.setAccessible(true)
////            List loaders = field.get(parentUcp)
////            loaders.add(0, fLoader);
////            pLoader.loadClass("CollectionKey")
////            pLoader.loadClass("EntityKey")
////            pLoader.loadClass("EntityUniqueKey")
////            pLoader.loadClass("QueryKey")
////            pLoader.loadClass("CacheKey")
////            pLoader.loadClass("FilterKey")
////            pLoader.loadClass("NaturalIdCacheKey")
////            urls.remove(0)
////            loaders.remove(0)
////        }
//        try {
//            if(NaturalIdCacheKey.getDeclaredField("dbNameCache")) {
//                NaturalIdCacheKey.dbNameCache = FilterKey.dbNameCache = CacheKey.dbNameCache = QueryKey.dbNameCache = EntityUniqueKey.dbNameCache = EntityKey.dbNameCache = CollectionKey.dbNameCache = MultiConnectDataSource.database
//            }
//        } catch (Throwable ignored) {}
//        WebApplicationContext context = super.initWebApplicationContext(servletContext)
//        Holders.servletContext.start_time = System.currentTimeMillis()
//        return context
//    }
//}
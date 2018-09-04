package com.webcommander.spring

import grails.web.mapping.UrlMappings
import org.grails.web.mapping.DefaultUrlMappingsHolder
import org.grails.web.mapping.UrlMappingsHolderFactoryBean
import org.grails.web.mapping.mvc.GrailsControllerUrlMappings
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Created by zobair on 17/11/13.*/
class ExtendedUrlMappingHolderFactory extends UrlMappingsHolderFactoryBean {
    PathMatcher pathMatcher = new AntPathMatcher()

    static interface ExtendedUrlMappings extends UrlMappings {
        boolean isExcluded(String uri, String method)
    }

    @Override
    void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet()

        Field field = UrlMappingsHolderFactoryBean.class.getDeclaredField("urlMappingsHolder")
        field.setAccessible(true)
        GrailsControllerUrlMappings mappingProxyHolder = field.get(this)
        DefaultUrlMappingsHolder mappingHolder = mappingProxyHolder.urlMappingsHolderDelegate

        field = DefaultUrlMappingsHolder.class.getDeclaredField("excludePatterns")
        field.setAccessible(true)
        List excludes = field.get(mappingHolder)
        Map methodBasedExcludes = [:]
        excludes.removeAll {
            if(it.contains(":")) {
                String[] parts = it.split(":")
                String method = parts[0].toLowerCase()
                List excludeForMethod = methodBasedExcludes[method] ?: (methodBasedExcludes[method] = [])
                excludeForMethod << parts[1]
                return true
            }
        }

        UrlMappings sndLevelMappingProxyHolder = Proxy.newProxyInstance(mappingHolder.class.classLoader, [ExtendedUrlMappings] as Class[], new InvocationHandler() {
            @Override
            Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.name == "isExcluded" && args.length == 2) {
                    return isExcluded(args[0], args[1]) || mappingHolder.isExcluded(args[0])
                }
                if(method.name == "matchAll" && args.length > 1 && args[1] instanceof String && isExcluded(args[0], args[1])) {
                    return DefaultUrlMappingsHolder.EMPTY_RESULTS
                }
                method.invoke(mappingHolder, *args)
            }
            
            boolean isExcluded(String uri, String method) {
                List excludeForMethod = methodBasedExcludes[method.toLowerCase()]
                if(!excludeForMethod) {
                    return false
                }
                for (String excludePattern : excludeForMethod) {
                    if(pathMatcher.match(excludePattern.toString(), uri)) {
                        return true
                    }
                }
                return false
            }
        })
        mappingProxyHolder.urlMappingsHolderDelegate = sndLevelMappingProxyHolder
    }
}
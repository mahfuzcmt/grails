package com.webcommander.util

import grails.util.Holders
import grails.web.Action
import org.grails.core.DefaultGrailsControllerClass
import org.grails.web.mapping.mvc.AbstractGrailsControllerUrlMappings
import org.grails.web.mapping.mvc.GrailsControllerUrlMappings

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

class ControllerMixin {

    static void mixinActions(Class baseControllerClass, Class mixinClass) {
        Class superClass = mixinClass
        GrailsControllerUrlMappings urlMappingHolder = AppUtil.getBean("urlMappingsTargetSource").target
        DefaultGrailsControllerClass controllerClass = Holders.grailsApplication.controllerClasses.find {it.clazz == baseControllerClass}
        Map actionMap = urlMappingHolder.mappingsToGrailsControllerMap
        def controller = Holders.grailsApplication.mainContext.getBean(baseControllerClass)
        controller.metaClass.mixin mixinClass
        while(superClass != null && superClass != Object.class && superClass != GroovyObject.class) {
            for(Method method : superClass.getMethods()) {
                if(Modifier.isPublic(method.getModifiers()) && method.getAnnotation(Action.class) != null) {

                    final String methodName = method.getName()
                    println("methodName: " + methodName)
                    actionMap.put(new AbstractGrailsControllerUrlMappings.ControllerKey(urlMappingHolder, null, controllerClass.logicalPropertyName, methodName, null), controllerClass)
                    controllerClass.@actions.put(methodName, Proxy.newProxyInstance(controllerClass.class.classLoader, [controllerClass.class.getDeclaredClasses().find {it.simpleName.endsWith("ActionInvoker")}] as Class[], new InvocationHandler() {
                        @Override
                        Object invoke(Object proxy, Method method2, Object[] args) throws Throwable {
                            return args[0]."${methodName}"()
                        }
                    }))
                }
            }
            superClass = superClass.getSuperclass()
        }
    }

}
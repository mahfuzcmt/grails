package com.webcommander.events

import com.webcommander.plugin.PluginManager
import com.webcommander.tenant.TenantContext

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by zobair on 08/09/13.*/
class AppEventManager {
    private static Map events = new ConcurrentHashMap()

    static void off(String name) {
        off(name, null, null)
    }

    static void on(String name, Closure handler) {
        on(name, -1, handler)
    }

    static void one(String name, Closure handler) {
        on(name, 1, handler)
    }

    static void off(String name, Closure handler) {
        off(name, null, handler)
    }

    static void on(String name, String namespace, Closure handler) {
        on(name, [namespace], -1, handler)
    }

    static void one(String name, String namespace, Closure handler) {
        on(name, [namespace], 1, handler)
    }

    static void on(String name, List namespaces, Closure handler) {
        on(name, namespaces, -1, handler)
    }

    static void one(String name, List namespaces, Closure handler) {
        on(name, namespaces, 1, handler)
    }

    static void on(String name, int repeatCount, Closure handler) {
        on(name, [], repeatCount, handler)
    }

    static void off(String name, String namespace) {
        off(name, namespace, null)
    }

    static void off(String name, String namespace, Closure handler) {
        if (name == "*") {
            Collection names = events.keySet()
            for (String ename : names) {
                off(ename, namespace, handler)
            }
            return
        }
        List registereds = events[name]
        if (!registereds) {
            return
        }
        if (!namespace) {
            if (handler) {
                registereds.removeAll(registereds.findAll { it.handler == handler })
            } else {
                events.remove(name)
            }
            return
        }
        registereds.removeAll(registereds.findAll { it.namespaces.contains(namespace) && (!handler || it.handler == handler) })
    }

    static void on(String name, List namespaces, int fireCount, Closure handler) {
        name = name.trim()
        if (name == "") {
            return
        }
        String[] names = name.split(" ")
        if (names.size() > 1) {
            names.each {
                on(it, namespaces, fireCount, handler)
            }
            return
        }
        if (fireCount < 1) {
            fireCount = -1
        }
        List registereds = events[name]
        if (!registereds) {
            registereds = events[name] = Collections.synchronizedList([])
        }
        def pluginClass = (handler.owner instanceof  Class) ? handler.owner : handler.owner.class
        Map event = [count: fireCount, tenant: fireCount > 0 ? TenantContext.currentTenant : null, handler: handler, namespaces: namespaces, plugin: PluginManager.getPluginName(pluginClass)]
        registereds.add(event)
    }

    static void on(String name, String namespace, int fireCount, Closure handler) {
        on(name, namespace ? [namespace] : [], fireCount, handler)
    }

    static void fire(String name) {
        fire(name, null)
    }

    static void fire(String name, List eventData, Closure responsePicker = null) {
        List registereds = events[name]
        if (!registereds) {
            return
        }
        List eventsToRemove = []
        registereds.each { event ->
            if(event.plugin) {
                if(!PluginManager.isInstalled(event.plugin)) {
                    return
                }
            }
            if(event.tenant && event.tenant != TenantContext.currentTenant) {
                return
            }
            if (event.count > -1) {
                event.count--
                if (event.count == 0) {
                    eventsToRemove.add(event)
                }
            }
            int paramCount = event.handler.getMaximumNumberOfParameters()
            List sendData = eventData ?: []
            def response
            if (paramCount == 0) {
                response = event.handler()
            } else if (paramCount == 1) {
                if (sendData.size() == 0) {
                    response = event.handler()
                } else if (sendData.size() >= 1) {
                    response = event.handler(sendData[0])
                }
            } else if (sendData.size() >= paramCount) {
                response = event.handler.call(sendData.subList(0, paramCount))
            } else {
                int diff = paramCount - sendData.size()
                for (int h = 0; h < diff; h++) {
                    sendData.add(null)
                }
                response = event.handler.call(sendData)
            }
            if (responsePicker) {
                responsePicker response
            }
        }
        registereds.removeAll(eventsToRemove)
    }
}
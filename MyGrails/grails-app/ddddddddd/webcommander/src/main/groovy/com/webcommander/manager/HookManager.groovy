package com.webcommander.manager

import com.webcommander.plugin.PluginManager

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by zobair on 05/12/13.
 */
class HookManager {
    private static Map<String, List<Hook>> hooks = new ConcurrentHashMap()

    static class Hook {
        Closure closure
        String plugin

        Hook() {}

        Hook(Closure closure) {
            this.closure = closure
            plugin = PluginManager.getPluginName(closure.owner.class)
        }
    }

    static register(String name, Closure handler) {
        def pluginClass = (handler.owner instanceof  Class) ? handler.owner : handler.owner.class
        register name, PluginManager.getPluginName(pluginClass), handler
    }

    static register(String name, String plugin, Closure closure) {
        String[] names = name.split(" ")
        names.each {
            name = it.trim()
            if (name) {
                Hook hook = plugin ? new Hook(closure: closure, plugin: plugin) : new Hook(closure)
                if (!hooks[name]) {
                    hooks[name] = [hook]
                } else {
                    hooks[name].add(hook)
                }
            }
        }
    }

    static hook(String name, Object primaryResponse, Object... states) {
        if (states == null) {
            states = [null]
        }
        if (hooks[name]) {
            hooks[name].each { hook ->
                if(hook.plugin && !PluginManager.isInstalled(hook.plugin)) {
                    return
                }
                Closure closure = hook.closure
                def args = [primaryResponse]
                args.addAll(states)
                int size = args.size()
                int paramCount = closure.getMaximumNumberOfParameters()
                if (paramCount == 1) {
                    primaryResponse = closure(size == 1 ? args[0] : args)
                } else if (size == paramCount) {
                    primaryResponse = closure.call(args)
                } else if (size > paramCount) {
                    List newArgs = args.subList(0, paramCount - 1)
                    newArgs.add(args.subList(paramCount - 1, size))
                    primaryResponse = closure.call(newArgs)
                } else {
                    int diff = paramCount - size
                    for (int h = 0; h < diff; h++) {
                        args.add(null)
                    }
                    primaryResponse = closure.call(args)
                }
                if (primaryResponse == null) {
                    primaryResponse = args[0]
                }
            }
        }
        return primaryResponse
    }
}
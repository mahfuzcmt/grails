package com.webcommander.interceptors

import com.webcommander.plugin.PluginManager

class BarInactivePluginInterceptor {

    int order = HIGHEST_PRECEDENCE

    BarInactivePluginInterceptor() {
        matchAll()
    }

    boolean before() {
        if(params.controller) {
            // need to check
//            String plugin = PluginManager.getPluginName(controllerClass.class)
//            if(plugin && !PluginManager.isInstalled(plugin)) {
//                response.status = 404
//                render "Url Not Valid"
//                return false
//            }
        }
        return true
    }
}
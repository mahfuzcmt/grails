package com.webcommander.plugin.visitor_listing

import com.webcommander.events.AppEventManager
import com.webcommander.manager.HookManager
import com.webcommander.plugin.visitor_listing.manager.VisitorListManager
import com.webcommander.util.AppUtil

import javax.servlet.ServletContext
import com.webcommander.plugin.PluginMeta

class BootStrap {

    def tenantInit = { tenant ->

    }

    def tenantDestroy = { tenant ->

    }

    def init = { servletContext ->
        AppEventManager.on("session-create", { session ->
            VisitorListManager.add(session)
        })

        AppEventManager.on("session-terminate", { session ->
            VisitorListManager.remove(session)
        })

        HookManager.register("visitor-list", {visitor ->
            return VisitorListManager.getsVisitors(AppUtil.params)
        })

        HookManager.register("visitor-count", {count ->
            return VisitorListManager.count()
        })
    }

    def destroy = {
    }
}
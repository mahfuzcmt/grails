package com.webcommander.listener

import com.webcommander.AppResourceTagLib
import com.webcommander.ApplicationTagLib
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import grails.util.Holders
import org.apache.commons.io.FileUtils

import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

class SessionManager implements HttpSessionListener {

    private static ApplicationTagLib _app

    private static ApplicationTagLib getApp() {
        if(_app) {
            return _app
        } else {
            return _app = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        }
    }

    private static AppResourceTagLib _appResource
    private static AppResourceTagLib getAppResource() {
        if(_appResource) {
            return _appResource
        } else {
            return _appResource = Holders.grailsApplication.mainContext.getBean(AppResourceTagLib)
        }
    }

    @Override
    void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.session
        AppEventManager.fire("session-create", [session])
    }

    @Override
    void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        println("------------------ Debug Session Timeout Log ------------------")
        HttpSession session = httpSessionEvent.session
        AppEventManager.fire("session-terminate", [session])
        AppEventManager.fire("session-terminate-" + session.id, [session])
        AppEventManager.off("*", "session-" + session.id)
        File folder = getTempFolder(session, false, false)
        if(folder && folder.exists()) {
            FileUtils.deleteDirectory(folder)
        }
        folder = getTempFolder(session, true, false)
        if(folder && folder.exists()) {
            FileUtils.deleteDirectory(folder)
        }
        CacheManager.removeCache(NamedConstants.CACHE.SCOPE_SESSION, session.id)
    }

    static String getTempPath(HttpSession session = null) {
        session = session ?: AppUtil.session
        return app.baseUrl() + "temp/" + session.id
    }

    static String getRelativeTempPath(HttpSession session = null) {
        session = session ?: AppUtil.session
        return app.relativeBaseUrl() + "temp/" + session.id
    }

    static String getCurrentResourceTempPath() {
        def session = AppUtil.session
        return app.relativeBaseUrl() + AppResourceTagLib.RESOURCES + "/${TenantContext.currentTenant}/temp/" + session.id
    }

    static String getSchemedTempPath(HttpSession session = null) {
        session = session ?: AppUtil.session
        return app.schemedBaseUrl() + "temp/" + session.id
    }

    static File getPublicTempFolder(boolean create = true) {
        return getTempFolder(null, false, create)
    }

    static File getProtectedTempFolder(boolean create = true) {
        return getTempFolder(null, true, create)
    }

    static File getTempFolder(HttpSession session = null, Boolean isProtected = true, boolean create = true) {
        session = session ?: AppUtil.session
        if(session) {

            println("")
            println("")
            println("------------------ Debug Session Log ------------------")
            println("appResource.getTempPhysicalPath() : " + appResource.getTempPhysicalPath())
            println("appResource.getResourcePhysicalPath(extension: AppResourceTagLib.TEMP) : " + appResource.getResourcePhysicalPath(extension: AppResourceTagLib.TEMP))
            println("------------------ End Debug Session Log ------------------")
            println("")
            println("")

            String tempLocation =  isProtected ? appResource.getTempPhysicalPath() : appResource.getResourcePhysicalPath(extension: AppResourceTagLib.TEMP)
            File base = new File(tempLocation, session.id)
            if(create) {
                base.mkdirs()
            } else if(!base.exists()) {
                return null
            }
            return base
        }
        return null
    }
}

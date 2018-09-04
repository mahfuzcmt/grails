package com.webcommander.plugin.visitor_listing.manager

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CacheManager
import com.webcommander.util.AppUtil
import javax.servlet.http.HttpSession

/**
 * Created by sajedur on 21/07/2014.
 */
class VisitorListManager {
    private static List<HttpSession> getSessionHolder() {
        List<HttpSession> sessions = CacheManager.get(NamedConstants.CACHE.TENANT_STATIC, "visitor_sessions")
        if(sessions == null) {
            CacheManager.cache(NamedConstants.CACHE.TENANT_STATIC, sessions = Collections.synchronizedList(new ArrayList<HttpSession>()), -1, "visitor_sessions")
        }
        return sessions
    }
    
    static void add(HttpSession session) {
        session.ip_address = AppUtil.request.ip
        sessionHolder.add(session)
    }

    static void remove(HttpSession session) {
        sessionHolder.remove(session)
    }

    static List<HttpSession> getsVisitors(Map params) {
        int max = params.max.toInteger()
        int fromIndex = params.offset.toInteger()
        int toIndex = (max == -1 || (max + fromIndex) >= sessionHolder.size()) ? sessionHolder.size() : (fromIndex + max)
        return sessionHolder.subList(fromIndex, toIndex)
    }

    static Integer count() {
        return sessionHolder.size()
    }
}
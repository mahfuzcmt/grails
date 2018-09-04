package com.webcommander.controllers.admin

import com.webcommander.manager.CacheManager
import com.webcommander.util.AppUtil
import org.hibernate.SessionFactory

class CacheController {

    def clear() {
        if(params.hibernate != null) {
            AppUtil.getBean(SessionFactory).cache.evictAllRegions()
        } else {
            CacheManager.clearAll()
        }
        render "OK"
    }
}

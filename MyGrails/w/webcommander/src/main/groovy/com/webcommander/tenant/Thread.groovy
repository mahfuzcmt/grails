package com.webcommander.tenant

/**
 * Created by LocalZobair on 31/01/2017.*/
class Thread extends java.lang.Thread {

    Thread(Runnable runnable) {
        super(runnable)
    }

    static Thread start(Closure runnable) {
        String tenantId = TenantContext.currentTenant
        Thread _thiz = new Thread({
            TenantContext.with tenantId, runnable
        })
        _thiz.start()
        return _thiz
    }
}
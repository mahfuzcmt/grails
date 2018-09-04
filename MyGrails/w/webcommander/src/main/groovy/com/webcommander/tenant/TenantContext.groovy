package com.webcommander.tenant

import com.webcommander.config.SiteConfig
import com.webcommander.plugin.PluginManager
import com.webcommander.protos.beans.TenantResolver
import com.webcommander.util.AppUtil
import grails.gorm.transactions.GrailsTransactionTemplate
import grails.util.Holders
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

import static com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES

/**
 * Created by touhid on 24/08/2016.*/
class TenantContext {
    private static List<String> tenantIds

    static String SINGLE_TENANT_SERVER = Holders.config.dataSource.server
    static String SINGLE_TENANT_NAME = Holders.config.dataSource.name
    static String SINGLE_TENANT_HOST = Holders.config.dataSource.host

    private static PlatformTransactionManager _transactionManager
    private static PlatformTransactionManager getTransactionManager() {
        return _transactionManager ?: (_transactionManager = AppUtil.getBean("transactionManager"))
    }

    private static final ThreadLocal<String> contextHolder = isMultiTenantEnabled() ? new ThreadLocal<String>() : new ThreadLocal<String>() {
        String cached_value

        @Override
        String get() {
            return this.cached_value ?: Holders.config.webcommander.singleTenant.tenantId
        }

        @Override
        void set(String t) {
        }

        @Override
        void remove() {
        }
    }

    static void setCurrentTenant(String deployCode) {
        contextHolder.set(deployCode)
    }

    static String getCurrentTenant() {
        return contextHolder.get() ?: resolveCurrentTenant()
    }

    static void clear() {
        contextHolder.remove()
    }

    static boolean isMultiTenantEnabled() {
        return Holders.config.webcommander.multiTenant.enabled
    }

    static with(String tenantId, Closure callable, Boolean withNewTransaction = false) {
        String cacheCode = currentTenant
        currentTenant = tenantId
        Closure modifiedCallable = callable
        if(withNewTransaction) {
            modifiedCallable = { _session ->
                DefaultTransactionDefinition definition  = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
                new GrailsTransactionTemplate(transactionManager, definition).execute(callable.curry(_session))
            }
        }
        try {
            AppUtil.getBean("hibernateDatastore").withNewSession modifiedCallable
        } finally {
            currentTenant = cacheCode
        }
    }

    static List<java.lang.Thread> eachParallel(Closure callable, Boolean withNewTransaction = true) {
        each callable, withNewTransaction, true
    }

    static eachParallelWithWait(Closure callable, Boolean withNewTransaction = true) {
        List<java.lang.Thread> threads = each callable, withNewTransaction, true
        threads.each { thread ->
            thread.join()
        }
    }

    static def each(Closure callable, Boolean withNewTransaction, Boolean isParallel) {
        int argCount = callable.maximumNumberOfParameters
        String plugin = PluginManager.getPluginName callable.owner.class
        Closure eachCallable = { code ->
            Closure innerCallable = callable.curry(code)
            with code, { _session, _status ->
                if(plugin && !PluginManager.isInstalled(plugin)) {
                    return
                }
                switch (argCount) {
                    case 1:
                        innerCallable()
                        break
                    case 2:
                        innerCallable _session
                        break
                    default:
                        innerCallable _session, _status
                }
            }, withNewTransaction
        }
        List<java.lang.Thread> allThreads
        if(isParallel) {
            allThreads = []
            Closure individualCallable = eachCallable
            eachCallable = { code ->
                allThreads << java.lang.Thread.start {
                    individualCallable code
                }
            }
        }
        getTenantIds().each eachCallable
        if(isParallel) {
            return allThreads
        }
        return tenantIds
    }

    static each(Closure callable, Boolean withNewTransaction = true) {
        each callable, withNewTransaction, false
    }

    static List<String> getTenantIds() {
        if (!tenantIds) {
            tenantIds = TenantContext.isMultiTenantEnabled() ? new TenantPropsResolver().tenantIds : [contextHolder.get()]
        }
        return tenantIds
    }

    private static resolveCurrentTenant() {
        String tenantId = AppUtil.getBean(TenantResolver)?.getCurrentTenantId()?: Holders.config.webcommander.singleTenant.tenantId
        currentTenant = tenantId
    }
}
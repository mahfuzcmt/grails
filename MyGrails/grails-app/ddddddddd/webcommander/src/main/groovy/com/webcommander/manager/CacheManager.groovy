package com.webcommander.manager

import com.webcommander.annotations.event.Event
import com.webcommander.annotations.event.EventHandler
import com.webcommander.tenant.TenantContext
import grails.util.Holders
import groovy.time.TimeDuration
import org.codehaus.groovy.runtime.NullObject
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by zobair on 05/12/13.*/
class CacheManager {
    private static class CacheNode {
        private ConcurrentHashMap sub = new ConcurrentHashMap()
        public Object entry
        public TimeDuration lifetime
        public Date lastseektime

        synchronized void setEntry(_entry) {
            entry = _entry
            lifetime = Holders.config.webcommander.cache.lifetime.hours
            lastseektime = Date.now()
        }

        synchronized void clearEntry() {
            entry = null
            lifetime = null
            lastseektime = null
        }

        boolean hasScope(scope) {
            sub.containsKey(scope)
        }

        CacheNode getAt(String id) {
            CacheNode node = sub[id]
            if (node?.expired) {
                minus(id)
                return null
            }
            return node
        }

        CacheNode putAt(String id, node) {
            sub[id] = node
        }

        CacheNode minus(id) {
            sub.remove(id)
        }

        void clear() {
            sub.clear()
        }

        List children() {
            sub.values() as List
        }

        boolean isExpired() {
            if(!entry || !lifetime) {
                return false
            }
            (Date.now() - lastseektime) > lifetime
        }

        boolean hasEntry() {
            return entry != null
        }
    }

    private static Map caches = new ConcurrentHashMap()

    static def cache(String scope, Object cacheable, String... identifier) {
        cache(scope, cacheable, null, *identifier)
    }

    static def cache(String scope, Object cacheable, Long threshold, String... identifier) {
        String scopeKey = [TenantContext.currentTenant, scope].join("#^#")
        CacheNode currentNode = caches[scopeKey]
        if (!currentNode) {
            currentNode = caches[scopeKey] = new CacheNode()
        }
        int size = identifier.size() - 1
        identifier.eachWithIndex { id, ind ->
            if (currentNode.hasScope(id)) {
                def nextNode = currentNode[id]
                if(!nextNode) {
                    currentNode = currentNode[id] = new CacheNode()
                    if(threshold) {
                        currentNode.lifetime = threshold.minutes
                    }
                } else {
                    currentNode = nextNode
                }
            } else {
                currentNode = currentNode[id] = new CacheNode()
                if(threshold) {
                    currentNode.lifetime = threshold.minutes
                }
            }
            if (ind == size) {
                currentNode.entry = cacheable
                if(threshold) {
                    if(threshold < 0) {
                        currentNode.lifetime = null
                    } else {
                        currentNode.lifetime = threshold.minutes
                    }
                }
            }
        }
        cacheable
    }

    static Object get(String scope, String... identifier) {
        String scopeKey = [TenantContext.currentTenant, scope].join("#^#")
        CacheNode currentNode = caches[scopeKey]
        if (!currentNode) {
            return null
        }
        int size = identifier.size() - 1
        int ind = -1
        def obj = identifier.findResult { id ->
            ind++
            currentNode = currentNode[id]
            if (!currentNode) {
                return NullObject.nullObject
            }
            if (ind == size) {
                currentNode.lastseektime = Date.now()
                return currentNode.entry
            }
        }
        if (obj.isNull()) {
            return null
        }
        return obj
    }

    static void removeCache(String scope, String... identifier) {
        String scopeKey = [TenantContext.currentTenant, scope].join("#^#")
        CacheNode currentNode = caches[scopeKey]
        int size = identifier.size() - 1
        if (size == 0) {
            caches.remove(scopeKey)
            return
        }
        if (!currentNode) {
            return
        }
        List currentNodes = [currentNode]
        identifier.eachWithIndex { id, ind ->
            if (ind == size) {
                if (id == "*") {
                    currentNodes*.clear()
                } else {
                    currentNodes*.minus(id)
                }
            } else {
                if (id == "*") {
                    currentNodes = currentNodes*.children().flatten().findAll { return it }
                } else {
                    currentNodes = currentNodes*.getAt(id).flatten().findAll { return it }
                }
            }
        }
    }

    static void clearExpiredCaches() {
        Closure iterator
        iterator = { k, CacheNode currentNode ->
            currentNode.sub.erase iterator
            if (currentNode.expired) {
                currentNode.clearEntry()
            }
            if (!(currentNode.hasEntry() || currentNode.sub.size())) {
                return true //have to remove
            }
        }
        caches.erase iterator
    }

    static void clearAll() {
        caches.keySet().findAll {it.startsWith(TenantContext.currentTenant + "#")}.each {
            caches.remove(it)
        }
    }

    static void removeAllCache(){
        caches.clear()
    }
}

@Component("repetitive_cache_clearer")
@EventHandler
class RepetitiveCacheClearer {
    @Event("clock-hourly-00-trigger")
    void clearExpiredCaches() {
        CacheManager.clearExpiredCaches()
    }
}
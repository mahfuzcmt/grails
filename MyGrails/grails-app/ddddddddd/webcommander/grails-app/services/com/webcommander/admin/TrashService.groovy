package com.webcommander.admin

import com.webcommander.events.AppEventManager
import com.webcommander.tenant.TenantContext

class TrashService {

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if(!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault {[:]}
        }
        return dynamic
    }

    static addConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].put(config.key, config.value)
        }
    }

    static removeConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].remove(config.key)
        }
    }

    public static Map getDomains() {
        return [
                Album:          "albumService",
                Article:        "contentService",
                Category:       "categoryService",
                Customer:       "customerService",
                Navigation:     "navigationService",
                Product:        "productService",
                Page:           "pageService",
                Operator:       "userService"
        ].with {it + getDYNAMIC_CONSTANT().domains}
    }
    public static Map getDomainNames() {
        return [:].with {it + getDYNAMIC_CONSTANT().domainNames}
    }

    def putObjectInTrash(String entity, def domainObj, String at1_reply) {
        if(!domainObj) {
            return false;
        }
        List dataList = [domainObj.id, at1_reply];
        AppEventManager.fire("before-" + entity + "-put-in-trash", dataList)
        domainObj.isInTrash = true;
        domainObj.merge();
        if(!domainObj.hasErrors()) {
            AppEventManager.fire(entity + "-put-in-trash", dataList)
            return true;
        }
        return false;
    }

    def restoreObjectFromTrash(def domainObj) {
        if(!domainObj) {
            return false;
        }
        domainObj.isInTrash = false;
        domainObj.merge();
        return !domainObj.hasErrors();
    }

}

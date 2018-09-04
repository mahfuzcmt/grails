package com.webcommander.plugin.variation.constant

import com.webcommander.tenant.TenantContext

public class DomainConstants {

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if (!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault { [:] }
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
    public static getVARIATION_REPRESENTATION() {
        return [
                TEXT: "text",
                COLOR: "color",
                IMAGE: "image"
        ] + DYNAMIC_CONSTANT.VARIATION_REPRESENTATION
    }

    public static Map getVARIATION_MODELS() {
        return [:] + DYNAMIC_CONSTANT.VARIATION_MODELS
    }
}
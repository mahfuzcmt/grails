package com.webcommander.constants

import com.webcommander.tenant.TenantContext

class LicenseConstants {

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

    static getPAYMENT_GATEWAY() {
        return [
                (DomainConstants.PAYMENT_GATEWAY_CODE.STORE_CREDIT): "allow_store_credit_feature"
        ].with {it + getDYNAMIC_CONSTANT().PAYMENT_GATEWAY}
    }

    static getPRODUCT_WIDGET() {
        [:].with {it + getDYNAMIC_CONSTANT().PRODUCT_WIDGET}
    }
}

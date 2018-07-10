package com.webcommander.util

import com.webcommander.tenant.TenantContext

class CacheKeyFactory {
    private static Closure singleTenantSuffix = { "single" }
    private static Closure multiTenantSuffix = TenantContext.&getCurrentTenant
    private static Closure cacheSuffix = TenantContext.isMultiTenantEnabled() ? multiTenantSuffix : singleTenantSuffix

    static String getCacheSuffix() {
        cacheSuffix()
    }
}

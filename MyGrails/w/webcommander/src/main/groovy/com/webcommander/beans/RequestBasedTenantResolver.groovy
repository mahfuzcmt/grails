package com.webcommander.beans

import com.webcommander.protos.beans.TenantResolver
import com.webcommander.tenant.TenantPropsResolver
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest

@Component
class RequestBasedTenantResolver implements TenantResolver {
    static TENANT_ID_REQUEST_HEADER = "tenant_id"

    @Override
    String getCurrentTenantId() {
        return null
    }

    String resolveCurrentTenant(HttpServletRequest request) {
        String id = request.getHeader(TENANT_ID_REQUEST_HEADER)
        return TenantPropsResolver.lookupForTenantId(id ?: request.serverName)
    }
}
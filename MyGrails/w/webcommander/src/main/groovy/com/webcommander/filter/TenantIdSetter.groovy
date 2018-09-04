package com.webcommander.filter

import com.webcommander.beans.RequestBasedTenantResolver
import com.webcommander.protos.beans.TenantResolver
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil

import javax.servlet.*
import javax.servlet.http.HttpServletResponse

/**
 * Created by sajedur on 13/07/2014.
 */
class TenantIdSetter implements Filter {

    RequestBasedTenantResolver _tenantResolver

    private RequestBasedTenantResolver getTenantResolver() {
        return _tenantResolver ?: (_tenantResolver = AppUtil.getBean(TenantResolver))
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String resolvedTenant = tenantResolver.resolveCurrentTenant(request)
        if(resolvedTenant) {
            TenantContext.currentTenant = resolvedTenant
            filterChain.doFilter(request, response)
            return
        }
        ((HttpServletResponse)response).status = 404
        response.outputStream.write("Server Is Down / Unauthorized Access".getBytes("UTF-8"))
    }

    @Override
    void destroy() {
    }
}
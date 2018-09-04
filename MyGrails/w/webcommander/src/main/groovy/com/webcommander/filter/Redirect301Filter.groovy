package com.webcommander.filter

import com.webcommander.config.RedirectMapping
import com.webcommander.spring.ExtendedUrlMappingHolderFactory
import com.webcommander.tenant.TenantContext
import com.webcommander.util.AppUtil
import org.springframework.aop.target.HotSwappableTargetSource

import javax.annotation.PostConstruct
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Created by sajedur on 13/07/2014.
 */
class Redirect301Filter implements Filter {

    public static Map<String, Map<String, String>> REDIRECT_ENTRIES = [:]

    private ExtendedUrlMappingHolderFactory.ExtendedUrlMappings urlMappings

    private String getRedirectUrl(HttpServletRequest request, String newUrl) {
        String redirectUrl
        if (newUrl.startsWith("http")) {
            redirectUrl = newUrl
        } else if (newUrl.startsWith("www.")) {
            redirectUrl = request.scheme + "://" + newUrl
        } else {
            redirectUrl = request.scheme + "://" + request.serverName
            if (request.serverPort != 80 || request.serverPort != 443) {
                redirectUrl += ":" + request.serverPort
            }
            if (!newUrl.startsWith("/")) {
                redirectUrl += request.contextPath == "/" ? request.contextPath : request.contextPath + "/"
            }
            redirectUrl += newUrl
        }
        return redirectUrl
    }

    private String getUrlPath(HttpServletRequest request) {
        return request.contextPath == "/" ? request.servletPath : request.contextPath + (request.servletPath != "/" ? request.servletPath : "")
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        urlMappings = AppUtil.getBean(HotSwappableTargetSource).target.urlMappingsHolderDelegate
    }

    private List<String> urlMatchPossibilities(ServletRequest servletRequest) {
        List possibilities = []
        String path = getUrlPath servletRequest
        possibilities.push servletRequest.scheme + "://" + servletRequest.serverName + ":" + servletRequest.serverPort + path
        possibilities.push "//" + servletRequest.serverName+ ":" + servletRequest.serverPort + path
        if((servletRequest.scheme == "http" && servletRequest.serverPort == 80) || (servletRequest.scheme == "https" && servletRequest.serverPort == 443)) {
            possibilities.push servletRequest.scheme + "://" + servletRequest.serverName + path
            possibilities.push "//" + servletRequest.serverName + path
        }
        possibilities.push path
        if(servletRequest.servletPath != "/") {
            possibilities.push servletRequest.servletPath.substring(1)
        } else {
            possibilities.push servletRequest.servletPath
        }
        return possibilities
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        Map redirectEntries = REDIRECT_ENTRIES[TenantContext.currentTenant]
        if(!redirectEntries) {
            redirectEntries = REDIRECT_ENTRIES[TenantContext.currentTenant] = [:]
            RedirectMapping.withNewSession {
                RedirectMapping.list().each {
                    if(it.scheme || it.host) {
                        redirectEntries[it.oldUrl] = it.newUrl
                    } else {
                        redirectEntries[it.path] = it.newUrl
                    }
                }
            }
        }

        if(!urlMappings.isExcluded(request.forwardURI, request.method) && redirectEntries.size() && !request.getParameterMap().containsKey("no-301")) {
            String newUrl = urlMatchPossibilities(request).findResult { path -> redirectEntries[path] }
            if (newUrl) {
                String redirectUrl = getRedirectUrl(request, newUrl)
                response.setStatus(301)
                response.setHeader("Location", redirectUrl)
                response.setHeader("Connection", "close")
            } else {
                filterChain.doFilter(request, response)
            }
        } else {
            filterChain.doFilter(request, response)
        }
    }

    @Override
    void destroy() {
    }
}

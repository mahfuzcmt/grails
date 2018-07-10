package com.webcommander

import com.webcommander.adapter.CommanderLifeCycleAdapter
import com.webcommander.adapter.WcUrlMappingsInfoHandlerAdapter
import com.webcommander.beans.SiteMessageSource
import com.webcommander.extension.grails.gorm.beans.DomainDataBinder
import com.webcommander.filter.Redirect301Filter
import com.webcommander.filter.TenantIdSetter
import com.webcommander.listener.SessionManager
import com.webcommander.tenant.TenantContext
import grails.plugins.*
import org.grails.spring.beans.factory.HotSwappableTargetSourceFactoryBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.core.Ordered

class WebcommanderGrailsPlugin extends Plugin {
    def title = "WebCommander Engine"
    def author = "A Rahman"
    def authorEmail = "arahman@webcommander.com"
    def description = '''This is the WebCommander Core Application Which has All Logic'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/wiki"

    @Override
    Closure doWithSpring() {{->
        siteMessageSource(SiteMessageSource)
        if(TenantContext.multiTenantEnabled) {
            tenantIdSetter(FilterRegistrationBean) {
                filter = bean(TenantIdSetter)
                urlPatterns = ['/*']
                order = Ordered.HIGHEST_PRECEDENCE
            }
        }
        redirect301Filter(FilterRegistrationBean) {
            filter = bean(Redirect301Filter)
            urlPatterns = ['/*']
            order = Ordered.HIGHEST_PRECEDENCE + 1
        }
        httpSessionServletListener(ServletListenerRegistrationBean) {
            listener = bean(SessionManager)
        }
        urlMappingsInfoHandler(WcUrlMappingsInfoHandlerAdapter)
        commanderLifeCycleAdapter(CommanderLifeCycleAdapter)
        grailsWebDataBinder(DomainDataBinder, ref("grailsApplication"))
    }}
}

package com.webcommander.plugin.visitor_listing.interceptors

import com.webcommander.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class VisitorInterceptor {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app;

    VisitorInterceptor() {
        match(controller: '*', action: '*')
    }

    boolean before() {
        String url = app.currentURL().toString();
        String forwardUrl = request.forwardURI;
        if (!(forwardUrl ==~ /^.+\..+$/)) {
            session.last_accessed_url = url;
        }
        true
    }

    boolean after() {
        true
    }

    void afterView() {
    }
}
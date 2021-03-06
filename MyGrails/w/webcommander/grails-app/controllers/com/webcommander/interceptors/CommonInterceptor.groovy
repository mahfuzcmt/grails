package com.webcommander.interceptors

import com.webcommander.ApplicationTagLib
import com.webcommander.AutoGeneratedPage
import com.webcommander.authentication.ControllerAnnotationParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.servlet.http.Cookie


class CommonInterceptor {
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app;

    int order = HIGHEST_PRECEDENCE + 1

    CommonInterceptor() {
        matchAll()
    }

    boolean before() {
        /* Flash Process */
        if (flash.param) {
            params.putAll(flash.param);
        }

        if (request.scheme == "http") {
            String pageId;
            if ((pageId = ControllerAnnotationParser.isAutoPage(controllerName, actionName))) {
                AutoGeneratedPage page = AutoGeneratedPage.findByName(pageId);
                if (page && page.isHttps) {
                    flash.param = params;
                    redirect(uri: app.currentURL(scheme: "https"))
                    return false;
                }
            }
        }

        /* Time Zone */
        if (!session.timezone || session.reset_timezone) {
            Cookie timezone = request.getCookies().find { it.name.startsWith("timezone") };
            if (timezone) {
                session.timezone = new SimpleTimeZone((timezone.value as int) * -60000, "visitor");
                session.reset_timezone = false
            } else {
                session.timezone = TimeZone.default
                session.reset_timezone = true
            }
        }
        true
    }

    boolean after() {
        /* Flash Process */
        if (flash.model && model) {
            model.putAll(flash.model);
        } else if (flash.model) {
            flash.model.each {
                request[it.key] = it.value
            }
        }
        true
    }

    void afterView() {
    }
}

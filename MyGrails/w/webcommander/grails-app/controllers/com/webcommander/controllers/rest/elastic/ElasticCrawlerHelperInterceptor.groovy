package com.webcommander.controllers.rest.elastic

import com.webcommander.util.AppUtil
import com.webcommander.util.security.SimpleEncrypter


class ElasticCrawlerHelperInterceptor {

    boolean before() {
        def controller = AppUtil.getBean(controllerClass.clazz)
        Long token = params.long("token")
        if(token == null) {
            response.setStatus(400)
            controller.rest([status: "error", message: "invalid.request"])
            return false
        }
        token = new SimpleEncrypter(token).getInfo()
        Long differ = System.currentTimeMillis() - token;
        if(differ > 300000) {
            response.setStatus(400)
            controller.rest([status: "error", message: "invalid.request"])
            return false
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

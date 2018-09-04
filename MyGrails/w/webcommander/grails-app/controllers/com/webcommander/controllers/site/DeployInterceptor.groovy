package com.webcommander.controllers.site

import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.security.InformationEncrypter


class DeployInterceptor {

    boolean before() {
        try {
            String authInfo = AppUtil.request.getHeader("auth-info");
            InformationEncrypter encrypter = new InformationEncrypter(authInfo, 1000 * 60 * 10);
            params.hiddenInfos = encrypter.getHiddenInfos();
        } catch (Exception ex) {
            throw new ApplicationRuntimeException("Unauthorized", "unauthorized")
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

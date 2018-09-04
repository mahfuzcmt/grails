package com.webcommander.controllers.admin.webcommerce

import com.webcommander.constants.DomainConstants
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil


class TaxAdminInterceptor {

    boolean before() {
        if(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.TAX, "configuration_type") != DomainConstants.TAX_CONFIGURATION_TYPE.MANUAL) {
            throw new ApplicationRuntimeException("not.allowed.to.do", ["configure tax"])
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}

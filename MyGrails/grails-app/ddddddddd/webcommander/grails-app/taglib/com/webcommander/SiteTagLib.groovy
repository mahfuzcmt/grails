package com.webcommander

import com.webcommander.beans.SiteMessageSource
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil


class SiteTagLib {
    static namespace = "site"

    SiteMessageSource siteMessageSource

    def message = { attrs, body ->
        if(attrs.code == null) {
            return ""
        }
        String message = siteMessageSource.convert(attrs.code, attrs.args ?: [], attrs.macros)
        if (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'false') {
            message = message.replaceAll("(?i)customer", "Member");
            message = message.replaceAll("(?i)customers", "Members");
        }
        out << message
    }

}

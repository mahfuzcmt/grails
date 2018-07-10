package com.webcommander.plugin.quote

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.util.AppUtil


class QuoteTagLib {
    static namespace = "quote"

    def getQuoteButtonInCart = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.QUOTE);
        if (config.enabled.toBoolean(false)) {
            out << "<a href='${app.relativeBaseUrl()}quote/save' ><span class='quote button et_cartp_quote' et-category='button' page='checkout'>${site.message(code: config.button_label)}</span></a>";
        }
    }
}

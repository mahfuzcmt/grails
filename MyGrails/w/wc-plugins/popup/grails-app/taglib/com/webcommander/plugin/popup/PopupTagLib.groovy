package com.webcommander.plugin.popup

import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.popup.constants.Constants
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.servlet.http.Cookie

class PopupTagLib {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    static namespace = "popup"

    def siteJSs = { attrs, body ->
        out << body()
        if(request.editMode != "true" && params.viewMode != "true") {
            out << app.javascript(src: 'plugins/popup/js/site-js/site.render-popup.js')
        }
    }

    Boolean isApplicable(Popup popup, Map config) {
        Cookie cookie = null;
        if(config.loading_frequency == Constants.FREQUENCY.ONE_TIME && (cookie = request.cookies.find { it.name.startsWith("site-popup")}) && config.config_version == cookie.value) {
            return false
        }
        return true
    }

    def renderInitialPopup = { attrs, body ->
        out << body()
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.POPUP)
        Popup popup = null
        if(!request.editMode && config.initial_popup && (popup = Popup.get(config.initial_popup)) && isApplicable(popup, config)) {
            out << g.include(view: "/plugins/popup/site/_popup.gsp", model: [popup: popup])
            Cookie cookie = new Cookie("site-popup", config.config_version);
            cookie.maxAge = Integer.MAX_VALUE;
            cookie.path = "/";
            response.addCookie(cookie);
        }
    }
}
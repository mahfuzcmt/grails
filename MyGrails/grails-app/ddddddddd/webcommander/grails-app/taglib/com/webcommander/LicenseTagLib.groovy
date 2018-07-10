package com.webcommander

import com.webcommander.manager.LicenseManager
import com.webcommander.models.License

class LicenseTagLib {
    static namespace = "license"

    def allowed = { attrs, body ->
        if (!attrs.id) {
            pageScope.license_check = true
            out << body()
            return;
        }
        Boolean isProvisionActive = LicenseManager.isProvisionActive();
        License license = isProvisionActive ? LicenseManager.license(attrs.id) : null
        if (!isProvisionActive || license) {
            pageScope.license_check = true
            out << body()
        } else {
            pageScope.license_check = false
        }
    }

    def otherwise = { attrs, body ->
        if (!pageScope.license_check) {
            out << body()
        }
    }

    def active = { attrs, body ->
        if (LicenseManager.isProvisionActive()) {
            out << body()
        }
    }

    def inactive = { attrs, body ->
        if (!LicenseManager.isProvisionActive()) {
            out << body()
        }
    }

    def message = { attrs, body ->
        out << LicenseManager.generateMessage(attrs.notification)
    }
}
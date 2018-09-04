package com.webcommander.admin

import com.webcommander.license.validator.ApiValidator
import com.webcommander.manager.LicenseManager

class LicenseService {

    def refresh() {
        if (LicenseManager.isProvisionActive()) {
            LicenseManager.fetchLicense()
            LicenseManager.validateLicense()
            ApiValidator.updateNextUpdateDate()
        }
    }
}

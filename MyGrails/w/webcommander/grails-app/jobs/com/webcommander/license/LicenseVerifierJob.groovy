package com.webcommander.license

import com.webcommander.manager.LicenseManager
import com.webcommander.tenant.TenantContext

/**
 * Created by zobair on 15/02/2015.
 */
class LicenseVerifierJob {
    static triggers = {
        simple name: 'newsletterTriggerK', startDelay: 60000, repeatInterval: 24 * 60 * 60 * 1000
    }

    def execute() {
        TenantContext.eachParallel {
            if(LicenseManager.isProvisionActive()) {
                LicenseManager.fetchLicense()
                LicenseManager.validateLicense()
            }
        }
    }
}

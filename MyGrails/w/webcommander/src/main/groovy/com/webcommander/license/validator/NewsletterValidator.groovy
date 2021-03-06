package com.webcommander.license.validator

import com.webcommander.manager.LicenseManager
import com.webcommander.models.License
import com.webcommander.webmarketting.Newsletter

/**
 * Created by zobair on 10/02/2015.
 */
class NewsletterValidator {
    public static String LICENSE_IDENTIFIER = "newsletter_limit"

    public static validateLicense() {
        License license = LicenseManager.license(LICENSE_IDENTIFIER)
        if(license) {
            int count = Newsletter.countByIsActive(true)
            if(count > license.limit) {
                if(license.isLimitExtensible) {
                    LicenseManager.sendNotice(1, LICENSE_IDENTIFIER, license.limit, "warning")
                } else {
                    LicenseManager.sendNotice(3, LICENSE_IDENTIFIER, license.limit, count, "error")
                }
            } else if(count >= license.limit * 0.7) {
                LicenseManager.sendNotice(2, LICENSE_IDENTIFIER, license.limit, "notice")
            }
            if(count <= license.limit){
                LicenseManager.hideObsoleteNotification(LICENSE_IDENTIFIER, "license-3")
            }
        }
    }

    public static deActivateExtras() {
        License license = LicenseManager.license(NewsletterValidator.LICENSE_IDENTIFIER)
        List<Newsletter> letters = Newsletter.createCriteria().list (offset: license.limit) {
            eq("isActive", true)
        }
        letters*.isActive = false
        letters*.save()
    }

    public static int getCurrentCount() {
        return Newsletter.countByIsActive(true)
    }
}
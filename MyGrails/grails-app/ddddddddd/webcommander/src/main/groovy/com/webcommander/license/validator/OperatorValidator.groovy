package com.webcommander.license.validator

import com.webcommander.admin.Operator
import com.webcommander.manager.LicenseManager
import com.webcommander.models.License
import com.webcommander.sso.ProvisionAPIService
import grails.util.Holders

/**
 * Created by zobair on 10/02/2015.
 */
class OperatorValidator {
    private static ProvisionAPIService _wizardService
    private static ProvisionAPIService getWizardService() {
        return _wizardService ?: (_wizardService = Holders.grailsApplication.mainContext.getBean(ProvisionAPIService))
    }

    public static String LICENSE_IDENTIFIER = "operator_limit"

    public static validateLicense() {
        License license = LicenseManager.license(LICENSE_IDENTIFIER)
        if(license) {
            int count = currentCount
            if(count > license.limit) {
                if(license.isLimitExtensible) {
                    LicenseManager.sendNotice(1, LICENSE_IDENTIFIER, license.limit, "warning")
                } else {
                    LicenseManager.sendNotice(3, LICENSE_IDENTIFIER, license.limit, count, "critical")
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
        License license = LicenseManager.license(OperatorValidator.LICENSE_IDENTIFIER)
        List<Operator> letters = Operator.createCriteria().list (offset: license.limit) {
            eq("isActive", true)
            ne("email", "implementer@webcommander")
        }
        letters*.isActive = false
        letters*.save()
    }

    public static int getCurrentCount() {
        List operatorUUIDs = wizardService.entityList().uuid
        if(operatorUUIDs) {
            return Operator.createCriteria().count {
                eq("isActive", true)
                ne("email", "implementer@webcommander")
                inList("uuid", operatorUUIDs)
            }
        } else {
            return 0;
        }
    }
}

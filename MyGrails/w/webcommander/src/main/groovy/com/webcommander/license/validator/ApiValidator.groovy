package com.webcommander.license.validator

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.models.License
import com.webcommander.util.AppUtil

/**
 * Created by zobair on 10/02/2015.
 */
class ApiValidator {
    public static String LICENSE_IDENTIFIER = NamedConstants.LICENSE_KEYS.API
    public static Date nextUpdateDate

    public static validateLicense() {
        License license = LicenseManager.license(LICENSE_IDENTIFIER)
        if(license) {
            if(license.limit) {
                if(AppUtil.api_monthly_hit_count > license.limit) {
                    if(license.isLimitExtensible) {
                        LicenseManager.sendNotice(1, LICENSE_IDENTIFIER, license.limit, "warning")
                    } else {
                        LicenseManager.sendNotice(1, LICENSE_IDENTIFIER, license.limit, "error")
                    }
                } else if(AppUtil.api_monthly_hit_count >= license.limit * 0.7) {
                    LicenseManager.sendNotice(2, LICENSE_IDENTIFIER, license.limit, "notice")
                }
                return false
            }
        }
    }

    public static void updateNextUpdateDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        Date lastDayOfMonth = cal.getTime().dayEnd;
        if(!nextUpdateDate || nextUpdateDate.getTime() < lastDayOfMonth.getTime()) {
            nextUpdateDate = lastDayOfMonth
            AppUtil.api_monthly_hit_count = 0
        }
    }
}
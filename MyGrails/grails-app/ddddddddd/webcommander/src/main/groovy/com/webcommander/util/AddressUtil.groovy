package com.webcommander.util

import com.webcommander.constants.DomainConstants
import com.webcommander.models.AddressData

/**
 * Created by sharif on 06/02/2018.
 */
class AddressUtil {

    static AddressData cleanLocation(AddressData address) {

        if (address) {
            address.postCode = null
            address.city = null
            address.countryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong()
            address.countryCode = null
            address.countryName = null
            address.stateId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_state").toLong()
            address.stateName = null
            address.stateCode = null
        }

        return address
    }

}

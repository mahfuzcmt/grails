package com.webcommander.validator

import com.webcommander.webcommerce.TaxProfile
import com.webcommander.webcommerce.TaxRule
import org.springframework.stereotype.Component

/**
 * Created by sharif on 06/02/2018.
 */
@Component
class TaxZoneSelectValidator implements Validator {

    boolean validate(def context) {

        if (context.selectedTaxProfileId) {
            TaxProfile profile = TaxProfile.get(context.long("selectedTaxProfileId"))
            for(TaxRule rule : profile.rules) {
                if (rule.zones && rule.zones.find { it.id == context.long("zoneId") }) {
                    return false
                }
            }
        }

        return true
    }

}

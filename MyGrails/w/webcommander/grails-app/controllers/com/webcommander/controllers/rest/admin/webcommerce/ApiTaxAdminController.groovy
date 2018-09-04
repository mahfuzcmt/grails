package com.webcommander.controllers.rest.admin.webcommerce

import com.webcommander.authentication.annotations.Restriction
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.TaxCode
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.webcommerce.TaxRule
import com.webcommander.webcommerce.TaxService

class ApiTaxAdminController extends RestProcessor {
    TaxService taxService

    @Restriction(permission = "tax.view.list")
    def profileList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<TaxProfile> profiles = taxService.getTaxProfiles(params)
        rest profiles: profiles
    }

    @Restriction(permission = "tax.view.list")
    def codeList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<TaxCode> codes = taxService.getTaxCodes(params)
        rest codes: codes
    }

    @Restriction(permission = "tax.view.list")
    def ruleList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        List<TaxRule> rules = taxService.getTaxRules(params)
        rest rules: rules
    }


}

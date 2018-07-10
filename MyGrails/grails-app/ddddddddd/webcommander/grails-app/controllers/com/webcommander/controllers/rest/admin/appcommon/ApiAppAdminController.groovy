package com.webcommander.controllers.rest.admin.appcommon

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import com.webcommander.webcommerce.PaymentGateway
import grails.converters.JSON

class ApiAppAdminController extends RestProcessor {
    AdministrationService administrationService
    ZoneService zoneService

    def defaultRegion() {
        Long defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong();
        Long defaultStateId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_state").toLong();
        rest([state: defaultStateId, country: defaultCountryId] as JSON);
    }

    def hasCity() {
        boolean cityExists = administrationService.isCityExistsForCountry(params.long("country") ?: 0);
        render([hasCity: cityExists] as JSON);
    }

    def statesForCountry() {
        def states = administrationService.getStatesForCountry(params.long("country") ?: 0);
        render(states.collect {
            [id: it.id, code: it.code, name: it.name]
        } as JSON)
    }

    def countries() {
        def countries = administrationService.getAllCountry();
        rest(countries: countries)
    }

    def zoneList() {
        params.max = params.max ?: "-1"
        params.offset = params.offset ?: "0"
        params.isDefault = false
        List<Zone> zones = zoneService.getZones(params)
        rest zones: zones
    }

    def paymentGatewayList() {
        List<PaymentGateway> gateways = PaymentGateway.createCriteria().list {}
        rest gateways: gateways
    }

    def licences() {
        String licence = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LICENSE, "licenseCache") ?: ""
        rest(JSON.parse(licence))
    }
}

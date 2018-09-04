package com.webcommander.controllers.rest.site.appcommon

import com.webcommander.admin.AdministrationService
import com.webcommander.admin.City
import com.webcommander.admin.State
import com.webcommander.admin.Zone
import com.webcommander.admin.ZoneService
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.util.RestProcessor
import grails.converters.JSON

class ApiAppController extends RestProcessor{
    AdministrationService administrationService
    ZoneService zoneService

    def countries() {
        def countries = administrationService.getAllCountry();
        rest(countries: countries)

    }

    def statesForCountry() {
        List<State> states = administrationService.getStatesForCountry(params.long("countryId") ?: 0);
        rest states: states
    }

    def cities() {
        List<City> cities = administrationService.getCities(params)
        rest cities: cities
    }

}

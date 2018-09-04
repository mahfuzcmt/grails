package com.webcommander.plugin.location.controllers.admin

import com.webcommander.admin.AdministrationService
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.location.LocationAddress
import com.webcommander.plugin.location.LocationService
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON

class LocationAdminController {

    AdministrationService administrationService
    LocationService locationService
    CommonService commonService

    def config() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCATION_WIDGET_DEFAULT_ADDRESS)
        render(view: "/plugins/location/admin/config", model: [configs: configs])
    }

    def fetchConfig() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCATION_WIDGET_DEFAULT_ADDRESS)
        render(configs as JSON)
    }

    def loadLocationAppView() {
        Integer count = locationService.getLocationsCount(params);
        params.max = params.max ?: "10";
        List<LocationAddress> locations = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            locationService.getLocations(params)
        }
        render(view: "/plugins/location/admin/appView", model:[locations: locations, count : count])
    }

    def createLocation() {
        LocationAddress location = params.id ? LocationAddress.get(params.long("id")) : new LocationAddress()
        Long defaultCountryId = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "default_country").toLong()
        def states = administrationService.getStatesForCountry(location?.country?.id ?: defaultCountryId)

        render (view: "/plugins/location/admin/infoEdit", model:[location: location, defaulCountryId: defaultCountryId, states: states])
    }

    def save() {
        params.remove("action")
        params.remove("controller")
        LocationAddress location = locationService.save(params)
        if(location) {
            render([status: "success", message: g.message(code: "location.save.success"), id: location.id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "location.save.failed")] as JSON)
        }
    }

    def delete() {
        try {
            if (locationService.deleteLocation(params.long("id"))) {
                render([status: "success", message: g.message(code: "location.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "location.delete.failure")] as JSON)
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def loadStateForCountry() {
        Long countryId = params.long("id") ?: 0
        def states = null;
        if (params.id) {
            states = administrationService.getStatesForCountry(countryId);
        }
        render(view: "/plugins/location/admin/stateFormFieldView", model: [states: states]);
    }

    def getCountryByCode() {
        def country;
        country = locationService.getCountryByCode(params);
        render([country: country] as JSON);
    }

    def getStateByCode() {
        Long countryId = params.long('countryId')
        def state
        state = locationService.getStateForCountry(countryId, params)
        render([state: state] as JSON)
    }
}
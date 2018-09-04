package com.webcommander.plugin.location.controllers.site

import com.webcommander.admin.Country
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.plugin.location.LocationAddress
import com.webcommander.plugin.location.LocationService
import com.webcommander.util.AppUtil
import grails.converters.JSON

class LocationPageController {

    LocationService locationService

    def autoComplete() {
        def locations = locationService.autoComplete(params)
        List<String>addresses = []

        locations.each {
            addresses.push(it.formattedAddress)
        }

        render(["query": "Unit", "suggestions": addresses] as JSON)
    }

    def getAddresses() {
        def locations
        locations = locationService.getAllAddress()
        render([locations: locations] as JSON)
    }

    def getAddressByName() {
        def address
        address = locationService.getAddress(params)
        render([address: address] as JSON)
    }

    def getDefaultAddress(){
        LocationAddress locationAddress = locationService.getFirstAddress()
        StoreDetail storeDetail = StoreDetail.first()
        if(locationAddress) {
            render([status: "success", location: locationAddress, showMarker: true] as JSON)
        } else if(storeDetail) {
            Country country = Country.get(storeDetail.address.countryId)
            render([status: "success", location: locationAddress, showMarker: false, addressFromStoreDetail: true, country: country.name] as JSON)
        } else {
            def defaultLocation = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCATION_WIDGET_DEFAULT_ADDRESS)
            locationAddress = new LocationAddress()
            locationAddress.name = defaultLocation.name
            locationAddress.city = defaultLocation.city
            locationAddress.postCode = defaultLocation.postCode
            locationAddress.formattedAddress = defaultLocation.formattedAddress
            locationAddress.locationAddress = defaultLocation.locationAddress
            locationAddress.latitude = defaultLocation.latitude
            locationAddress.longitude = defaultLocation.longitude
            locationAddress.contactEmail = defaultLocation.contactEmail
            locationAddress.description = defaultLocation.description
            locationAddress.discard()
            render([status: "success", location: locationAddress, showMarker: false] as JSON)
        }
    }


}

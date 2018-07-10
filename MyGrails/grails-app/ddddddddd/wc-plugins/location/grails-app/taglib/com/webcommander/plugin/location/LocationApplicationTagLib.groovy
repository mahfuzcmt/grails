package com.webcommander.plugin.location

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class LocationApplicationTagLib {
    static namespace = "location"

    def adminJSs = {attrs, body ->
        out << body()
        out << "<script src='https://maps.googleapis.com/maps/api/js?key=${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.LOCATION_WIDGET_DEFAULT_ADDRESS, "api_key")}&v=3&libraries=places'></script>"
        out << app.javascript(src: 'plugins/location/js/admin/jquery.autocomplete.js')
    }

    def addressListDropdown = {attrs, body ->
        def allLocationList = LocationAddress.findAll()

        def locations = []

        allLocationList.each {
            locations.add(it.locationAddress)
        }
        out << g.select(from: locations, noSelection: null, keys: locations, name: "${attrs["name"]}", id: "${attrs["id"]}", value: attrs["value"], class: "large")
    }
}

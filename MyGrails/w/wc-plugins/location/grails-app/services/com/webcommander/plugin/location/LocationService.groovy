package com.webcommander.plugin.location

import com.webcommander.admin.Country
import com.webcommander.admin.State
import com.webcommander.converter.json.JSON
import com.webcommander.util.AppUtil
import grails.web.servlet.mvc.GrailsParameterMap

class LocationService {

    private Closure getCriteriaClosureForLocation(Map params) {
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
        }
        return closure;
    }

    Integer getLocationsCount (Map params) {
        return LocationAddress.createCriteria().count {
            and getCriteriaClosureForLocation(params);
        }
    }

    List<LocationAddress> getLocations (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return LocationAddress.createCriteria().list(listMap) {
            and getCriteriaClosureForLocation(params);
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<LocationAddress> autoComplete(GrailsParameterMap params) {
        String queryLocationAddress = params.query.trim().encodeAsLikeText()
        List<LocationAddress> allAddres = LocationAddress.createCriteria().list() {
            ilike("city", "%${queryLocationAddress}%")
        }
        if(queryLocationAddress.isNumber()){
            Integer number = queryLocationAddress.toInteger()
            String lowerNumber = (number - 10).toString()
            lowerNumber = lowerNumber.substring(0, (lowerNumber.length() - 1)) + "%"
            String higherNumber = (number + 10).toString()
            higherNumber = higherNumber.substring(0, (higherNumber.length() - 1)) + "%"
            String searchString = queryLocationAddress.substring(0, (queryLocationAddress.length() - 1)) + "%"
            List<LocationAddress> locationAddresses = LocationAddress.createCriteria().list() {
                or {
                    like("postCode", higherNumber)
                    like("postCode", lowerNumber)
                    like("postCode", searchString)
                }

            }
            locationAddresses.each {
                allAddres.add(it)
            }
        }
        if(params.name == "Australia") {
            return allAddres.findAll {it.country.name == params.name.trim()}
        } else {
            return allAddres.findAll {it.country.name != "Australia"}
        }
    }

    def save(Map params) {
        Long id = params.id.toLong(0)
        LocationAddress location = id ? LocationAddress.get(id) : new LocationAddress()
        location.name = params.name.trim()
        location.locationHeadingName = params.locationHeadingName
        location.locationAddress = params.locationAddress
        location.formattedAddress = params.formattedAddress
        location.postCode = params.postCode
        location.city = params.city
        location.contactEmail = params.contactEmail
        location.phoneNumber = params.phoneNumber
        location.textHeading = params.textHeading
        location.description = params.description
        location.linkText = params.linkText
        location.webpageUrl = params.webpageUrl
        location.latitude = params.latitude
        location.longitude = params.longitude
        location.country = Country.get(params.country.id)
        location.state = State.get(params.state ? params.state.id : 0)
        location.showEmailInDetails = (params.showEmailInDetails == "on")
        location.showPhoneNumberInDetails = (params.showPhoneNumberInDetails == "on")
        location.showTextHeadingInDetails = (params.showTextHeadingInDetails == "on")
        location.showWebpageInDetails = (params.showWebpageInDetails == "on")
        location.save(flush: true)

        if(!location.hasErrors()) {
            return location
        }
        return null
    }

    def deleteLocation(Long id) {
        LocationAddress location = LocationAddress.get(id);
        try {
            location.delete(flush: true);
        } catch (Throwable t) {
            return false
        }
        return true
    }

    def getCountryByCode(GrailsParameterMap params) {
        def country = Country.createCriteria().list {
            eq("isActive", true)
            eq("code", params.countryCode)
        }
        return country
    }

    def getStateForCountry(Long id, GrailsParameterMap params) {
        def states = State.createCriteria().list {
            eq("country.id", id)
            eq("isActive", true)
            eq("code", params.stateCode)
        }
        return states
    }

    def getAddress(GrailsParameterMap params) {
        List<LocationAddress> locations = LocationAddress.createCriteria().list() {
            eq("formattedAddress", params.name)
        }
        if(locations.size() == 0){
            locations = LocationAddress.createCriteria().list() {
                eq("postCode", params.name)
            }
        }
        return locations
    }

    def getFirstAddress(){
        return LocationAddress.first()
    }

    def getAllAddress() {
        def locations = LocationAddress.createCriteria().list() {

        }
        return locations
    }
}

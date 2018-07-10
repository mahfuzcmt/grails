package com.webcommander.plugin.location

import com.webcommander.admin.Country
import com.webcommander.admin.State

class LocationAddress {

    Long id
    String name
    String locationHeadingName
    String locationAddress
    String formattedAddress
    String postCode
    String city
    String contactEmail
    String phoneNumber
    String textHeading
    String description
    String linkText
    String webpageUrl
    String latitude
    String longitude
    Boolean showEmailInDetails
    Boolean showPhoneNumberInDetails
    Boolean showTextHeadingInDetails
    Boolean showWebpageInDetails

    Date created
    Date updated

    Country country
    State state

    static constraints = {
        name(nullable: true)
        locationHeadingName(nullable: true)
        locationAddress(nullable: true)
        formattedAddress(nullable: true)
        postCode(nullable: true)
        state(nullable: true)
        city(nullable: true)
        contactEmail(blank: false, email: true)
        phoneNumber(nullable: true)
        textHeading(nullable: true)
        description(nullable: true)
        linkText(nullable: true)
        webpageUrl(nullable: true)
        latitude(nullable: true)
        longitude(nullable: true)
        showEmailInDetails(nullable: true)
        showPhoneNumberInDetails(nullable: true)
        showTextHeadingInDetails(nullable: true)
        showWebpageInDetails(nullable: true)
    }

    static mapping = {
        description type: "text"
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    @Override
    int hashCode(){
        if(id){
            return ("LocationAddress: " + id).hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if(! (o instanceof LocationAddress)) {
            return false
        }
        if(id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }
}
package com.webcommander.webcommerce

import com.webcommander.admin.Country
import com.webcommander.admin.State

class Address {

    Long id
    String firstName
    String lastName
    String addressLine1
    String addressLine2
    String postCode
    String city
    String phone
    String mobile
    String fax
    String email

    Country country
    State state

    static belongsTo = [country : Country, state: State]

    static constraints = {
        addressLine1(maxSize: 500)
        addressLine2(nullable: true, maxSize: 500)
        postCode(nullable: true, maxSize: 50)
        state(nullable: true)
        city(nullable: true, maxSize: 50)
        phone(nullable: true, maxSize: 50)
        email(maxSize: 50, nullable: true)
        firstName(size: 2..100)
        lastName(nullable: true)
        mobile(nullable: true)
        fax(nullable: true)
    }

    static transients = ['getFullName']

    @Override
    int hashCode(){
        if(id){
            return ("Address: " + id).hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if(! (o instanceof Address)) {
            return false
        }
        if(id && o.id) {
            return id == o.id
        }
        return super.equals(o);
    }

    String getFullName() {
        return this.firstName + " " + (this.lastName ?: "")
    }
}

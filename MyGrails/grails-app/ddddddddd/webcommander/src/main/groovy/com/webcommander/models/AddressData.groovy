package com.webcommander.models

import com.webcommander.admin.Customer
import com.webcommander.config.StoreDetail
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Address
import org.springframework.beans.BeanUtils

/**
 * Created by zobair on 15/01/14.*/
class AddressData implements Serializable {
    public static final long serialVersionUID = 111111111111;

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

    Long countryId
    String countryCode
    String countryName

    Long stateId
    String stateName
    String stateCode

    public AddressData(Address address) {
        BeanUtils.copyProperties(address, this, "class", "metaClass", "country", "state");
        countryId = address.country.id
        countryCode = address.country.code
        countryName = address.country.name
        if(address.state) {
            stateId = address.state.id
            stateCode = address.state.code
            stateName = address.state.name
        }
    }

    public AddressData() {
    }

    String getFullName() {
        return firstName + ( lastName ? " " + lastName : "")
    }

    String getAddressLine() {
        return addressLine1 + ( addressLine2 ? ", " + addressLine2 : "")
    }

    static AddressData resolveAddress() {
        AddressData selectedAddress = AppUtil.session.effective_billing_address

        if (selectedAddress) {
            return selectedAddress
        } else if (AppUtil.session && AppUtil.session.customer) {
            Customer customer = Customer.get(AppUtil.session.customer)
            Address customerAddress = customer.activeBillingAddress ?: customer.address
            if (customerAddress) {
                selectedAddress = new AddressData(customerAddress)
            }
        } else if (AppUtil.session && AppUtil.session.checkout_as_guest && AppUtil.session.effective_billing_address) {
            selectedAddress = AppUtil.session.effective_billing_address
        } else {
            selectedAddress = new AddressData(StoreDetail.first().address)
        }

        if (!selectedAddress) {
            return null
        }
        return selectedAddress
    }
}

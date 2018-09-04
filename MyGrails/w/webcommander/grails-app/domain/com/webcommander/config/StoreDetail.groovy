package com.webcommander.config

import com.webcommander.admin.Country
import com.webcommander.admin.State
import com.webcommander.events.AppEventManager
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.webcommerce.Address

class StoreDetail extends AbstractStaticResource {

    Long id
    String name

    String abn
    String additionalInfo
    String image
    String imageBaseUrl

    Address address

    boolean isDefault = false
    String url
    String identifire
    String location


    static transients = ['baseUrl', 'resourceName', 'relativeUrl']

    static constraints = {
        name(size: 4..100, unique: true)
        abn(nullable: true)
        image(nullable: true)
        imageBaseUrl(nullable: true)
        additionalInfo(nullable: true, maxSize: 1000)
        url(nullable: true)
        identifire(nullable: false, unique: true)
        location(nullable: true)
    }

    public static void initialize() {
        def _init = {
            if(StoreDetail.count() == 0) {
                def data = [
                    name: 'My Company',
                    country: Country.findByCode("AU"),
                    state: State.findByCode("VIC"),
                    email: 'name@company.com',
                    addressLine1: "Address 1",
                    postCode: "1234",
                    phone: "1300 123 456",
                    isDefault: true,
                    identifire: "default"
                ]
                Address storeAddress = new Address(firstName: data.name, addressLine1: data.addressLine1, postCode: data.postCode, country: data.country, state: data.state, email: data.email,
                        phone: data.phone).save()
                new StoreDetail(name: data.name, address: storeAddress, isDefault: data.isDefault, identifire: data.identifire).save();
            }
        }
        if(State.count()) {
            _init()
        } else {
            AppEventManager.one("state-bootstrap-init", "bootstrap-init", _init)
        }
    }

    @Override
    String getBaseUrl(){
        return super.getBaseUrl()
    }

    @Override
    void setBaseUrl(String baseUrl) {
        this.imageBaseUrl = baseUrl
    }

    @Override
    String getResourceName() {
        return image
    }

    @Override
    void setResourceName(String resourceName) {
        this.image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getStoreRelativeUrl()
    }
}

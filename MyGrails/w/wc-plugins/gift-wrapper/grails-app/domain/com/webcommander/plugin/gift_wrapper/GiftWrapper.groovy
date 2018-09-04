package com.webcommander.plugin.gift_wrapper

import com.webcommander.calculator.TaxCalculator
import com.webcommander.constants.DomainConstants
import com.webcommander.models.blueprints.AbstractStaticResource
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.TaxCode

class GiftWrapper extends AbstractStaticResource {

    Long id

    String name
    Double price
    String description
    String image
    String baseUrl

    Boolean isAllowGiftMessage = true
    Boolean isVisibleToCustomer = true

    Date created
    Date updated

    static transients = ['baseUrl', 'resourceName', 'relativeUrl']

    static constraints = {
        name(nullable: true, size: 2..100)
        price(nullable: true, maxSize: 100)
        description(nullable: true, maxSize: 1000)
        image(nullable: true)
        baseUrl(nullable: true)
    }

    static mapping = {
        description length: 1000
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
    int hashCode() {
        if (id) {
            return ("GiftWrapper: " + id).hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof GiftWrapper)) {
            return false
        }
        if (id && o.id) {
            return id == o.id
        }
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl
    }

    @Override
    String getResourceName() {
        return this.image
    }

    @Override
    void setResourceName(String resourceName) {
        this.image = resourceName
    }

    @Override
    String getRelativeUrl() {
        return GiftWrapperResourceTagLib.getGiftWrapperImageRelativeUrl(id)
    }

    Double getActualPrice() {
        return TaxCalculator.getActualPriceFromEnteredWithTax(price)
    }

}

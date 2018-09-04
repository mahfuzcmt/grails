package com.webcommander.plugin.enterprise_variation

import com.webcommander.models.blueprints.AbstractStaticResource

class VariationProductImage extends AbstractStaticResource {

    Long id
    String name
    String baseUrl
    String altText
    Integer idx

    static belongsTo = [evariationDetails: EvariationDetails]

    static transients = ['findUrlInfix', 'resourceName', 'relativeUrl']

    static constraints = {
        altText(nullable: true, maxSize: 250)
        name(maxSize: 250)
        baseUrl(nullable: true)
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof VariationProductImage)) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("VariationProductImage: " + id).hashCode()
        }
        return super.hashCode()
    }

    String findUrlInfix() {
        return appResource.findVariationProductUrlInfix(getTenantId(), evariationDetails.id)
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }


    @Override
    String getResourceName() {
        return name
    }

    @Override
    void setResourceName(String resourceName) {
        this.name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getVariationProductRelativeUrl(evariationDetails.id)
    }

    @Override
    List<String> getPrefixes() {
        return ["150", "300", "450", "600", "900"]
    }
}
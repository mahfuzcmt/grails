package com.webcommander.plugin.variation

import com.webcommander.models.blueprints.AbstractStaticResource

public class VariationOption extends AbstractStaticResource {
    Long id
    Integer idx = 0
    String label
    String value
    String imageBaseUrl
    VariationType type

    static belongsTo = [type: VariationType]

    static transients = ['baseUrl', 'resourceName', 'relativeUrl']

    static constraints = {
        label(maxSize: 250)
        value(maxSize: 250)
        imageBaseUrl(nullable: true)
    }

    @Override
    String getBaseUrl() {
        return super.getBaseUrl()
    }

    @Override
    void setBaseUrl(String baseUrl) {
        imageBaseUrl = baseUrl
    }

    @Override
    String getResourceName() {
        return value
    }

    @Override
    void setResourceName(String resourceName) {
        value = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getVariationRelativeUrl(id)
    }
}
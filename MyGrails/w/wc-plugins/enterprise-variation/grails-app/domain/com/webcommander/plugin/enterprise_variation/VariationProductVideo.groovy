package com.webcommander.plugin.enterprise_variation

import com.webcommander.models.blueprints.AbstractStaticResource

class VariationProductVideo extends AbstractStaticResource {

    Long id
    String name
    String title
    String description
    String baseUrl
    Integer idx

    static belongsTo = [evariationDetails: EvariationDetails]

    static transients = ['findUrlInfix', "thumbImage"]

    static constraints = {
        title(nullable: true)
        baseUrl(nullable: true)
        description(nullable: true, maxSize: 250)
    }

    String findUrlInfix() {
        return appResource.getProductVideoInfix(getTenantId(), evariationDetails.id)
    }

    String getThumbImage() {
        String thumb = name.substring(0, name.lastIndexOf(".")) + ".jpg"
        return app.customResourceBaseUrl() + findUrlInfix() + appResource.VIDEO_THUMB + "/" + thumb
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

}
package com.webcommander.plugin.general_event

import com.webcommander.models.blueprints.AbstractStaticResource

class VenueLocationImage extends AbstractStaticResource {

    Long id
    String name
    String baseUrl

    Integer idx

    static belongsTo = [venueLocation: VenueLocation]

    static transients = ['resourceName', 'relativeUrl']

    static mapping = {
        autoImport false
        table("general_event_venue_location_image")
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof GeneralEvent)) {
            return false;
        }
        if (this.id) {
            return id == obj.id
        }
        return super.equals(obj)
    }

    @Override
    String getBaseUrl() {
        return this.baseUrl
    }

    @Override
    String getResourceName() {
        return this.name
    }

    @Override
    void setResourceName(String resourceName) {
        this.name = resourceName
    }

    @Override
    String getRelativeUrl() {
        return appResource.getVenueLocationImageRelativeUrl(venueLocation?.id)
    }

    @Override
    List<String> getPrefixes() {
        return ["150", "300", "600"]
    }
}

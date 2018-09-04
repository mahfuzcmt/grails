package com.webcommander.plugin.general_event

import com.webcommander.models.blueprints.AbstractStaticResource

class GeneralEventImage extends AbstractStaticResource {

    Long id
    String name
    String baseUrl
    GeneralEvent event

    Integer idx

    static belongsTo = [GeneralEvent]

    static transients = ['resourceName', 'relativeUrl']

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
        return appResource.getGeneralEventImageRelativeUrl(event?.id)
    }

    @Override
    List<String> getPrefixes() {
        return ["150", "300", "600"]
    }
}

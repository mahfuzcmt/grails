package com.webcommander.plugin.event_management

import com.webcommander.models.blueprints.AbstractStaticResource

class EventImage extends AbstractStaticResource {

    Long id
    String name
    String baseUrl
    Event event

    Integer idx

    static belongsTo = [Event]

    static transients = ['resourceName', 'relativeUrl']

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof Event)) {
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
        return appResource.getEventImageRelativeUrl(event?.id)
    }

    @Override
    List<String> getPrefixes() {
        return ["100", "300"]
    }
}
